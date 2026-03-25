package com.example.email_listener_service.service;

import com.example.email_listener_service.dto.EmailMessage;
import com.example.email_listener_service.kafka.EmailKafkaProducer;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailIngestionService {

    private final EmailKafkaProducer emailKafkaProducer;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("[ \\t\\x0B\\f\\r]+");
    private static final Pattern MULTI_NEWLINE_PATTERN = Pattern.compile("\\n{3,}");
    private static final Pattern MIME_MULTIPART_MARKER = Pattern.compile("jakarta\\.mail\\.internet\\.MimeMultipart@[0-9a-fA-F]+");

    public void processEmail(MimeMessage message) {
        try {
            Address[] froms = message.getFrom();
            String sender = froms == null ? "Unknown" : froms[0].toString();
            String subject = normalizeText(message.getSubject() == null ? "" : message.getSubject());
            String body = normalizeText(extractText(message));
            if (body.isBlank()) {
                body = "(No body content extracted)";
            }

            String messageId = message.getMessageID();
            if (messageId == null || messageId.isBlank()) {
                messageId = "msg-" + System.currentTimeMillis();
            }

            log.info("Parsed IMAP email messageId={} subject='{}' bodyPreview='{}'",
                    messageId,
                    subject,
                    body.length() > 120 ? body.substring(0, 120) + "..." : body);

            EmailMessage emailMessage = EmailMessage.builder()
                    .gmailId(messageId)
                    .threadId(messageId)
                    .sender(sender)
                    .subject(subject)
                    .body(body)
                    .receivedAt(Instant.now())
                    .build();

            emailKafkaProducer.sendEmail(emailMessage);
        } catch (Exception e) {
            log.error("Error processing incoming IMAP email", e);
        }
    }

    private String extractText(Part part) {
        return extractCandidate(part).text();
    }

    private TextCandidate extractCandidate(Part part) {
        try {
            if (part instanceof BodyPart bodyPart && isAttachment(bodyPart)) {
                return TextCandidate.empty();
            }

            if (part.isMimeType("text/plain")) {
                Object content = part.getContent();
                String text = readTextContent(content);
                return TextCandidate.plain(text);
            }
            if (part.isMimeType("text/html")) {
                Object content = part.getContent();
                String text = stripHtml(readTextContent(content));
                return TextCandidate.html(text);
            }
            if (part.isMimeType("message/rfc822")) {
                Object content = part.getContent();
                if (content instanceof Message nestedMessage) {
                    return extractCandidate((Part) nestedMessage);
                }
            }
            if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
                TextCandidate best = TextCandidate.empty();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    TextCandidate child = extractCandidate(bodyPart);
                    if (child.isBetterThan(best)) {
                        best = child;
                    }
                }
                return best;
            }
        } catch (Exception e) {
            log.warn("Failed to parse MIME part, continuing with empty text", e);
        }
        return TextCandidate.empty();
    }

    private boolean isAttachment(BodyPart bodyPart) {
        try {
            String disposition = bodyPart.getDisposition();
            if (disposition != null) {
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                    return true;
                }
                if (Part.INLINE.equalsIgnoreCase(disposition)
                        && bodyPart.getFileName() != null
                        && !bodyPart.getFileName().isBlank()) {
                    return true;
                }
            }
            return bodyPart.getFileName() != null && !bodyPart.getFileName().isBlank();
        } catch (Exception ignored) {
            return false;
        }
    }

    private String stripHtml(String html) {
        return html.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("(?i)<li>", "- ")
                .replaceAll("(?i)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?i)<script[^>]*>.*?</script>", " ");
    }

    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        if (MIME_MULTIPART_MARKER.matcher(input.trim()).matches()) {
            return "";
        }
        String withoutTags = HTML_TAG_PATTERN.matcher(input).replaceAll(" ");
        String normalizedLineEndings = withoutTags.replace("\r\n", "\n").replace('\r', '\n');
        String compactSpaces = MULTI_SPACE_PATTERN.matcher(normalizedLineEndings).replaceAll(" ");
        String compactNewlines = MULTI_NEWLINE_PATTERN.matcher(compactSpaces).replaceAll("\n\n");
        return compactNewlines.trim();
    }

    private String readTextContent(Object content) {
        if (content == null) {
            return "";
        }
        try {
            if (content instanceof String str) {
                return str;
            }
            if (content instanceof byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            if (content instanceof InputStream inputStream) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("Failed to decode text content", e);
        }
        return content.toString();
    }

    private record TextCandidate(String text, int quality) {
        static TextCandidate empty() {
            return new TextCandidate("", 0);
        }

        static TextCandidate plain(String text) {
            if (text == null || text.isBlank()) {
                return empty();
            }
            return new TextCandidate(text, 3);
        }

        static TextCandidate html(String text) {
            if (text == null || text.isBlank()) {
                return empty();
            }
            return new TextCandidate(text, 2);
        }

        boolean isBetterThan(TextCandidate other) {
            if (this.quality > other.quality) {
                return true;
            }
            return this.quality == other.quality && this.text.length() > other.text.length();
        }
    }
}
