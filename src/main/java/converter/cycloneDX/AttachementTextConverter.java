package converter.cycloneDX;

import converter.Converter;
import org.cyclonedx.model.AttachmentText;

public class AttachementTextConverter implements Converter<String, AttachmentText> {
    @Override
    public org.cyclonedx.model.AttachmentText convert(String text) {
        if (text == null) return null;

        var attachmentText = new org.cyclonedx.model.AttachmentText();
        attachmentText.setText(text);
        attachmentText.setEncoding("utf-8");
        return attachmentText;
    }
}
