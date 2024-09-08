package converter.cycloneDX;

import converter.Converter;

public class ExternalReferenceConverter implements Converter<data.ExternalReference, org.cyclonedx.model.ExternalReference> {
    @Override
    public org.cyclonedx.model.ExternalReference convert(data.ExternalReference externalReference) {
        if (externalReference == null) return null;

        var externalReference1 = new org.cyclonedx.model.ExternalReference();
        externalReference1.setType(getExternalReferenceType(externalReference.type()));
        externalReference1.setUrl(externalReference.url());
        externalReference1.setComment(externalReference.comment());

        return externalReference1;
    }

    private org.cyclonedx.model.ExternalReference.Type getExternalReferenceType(String type) {
        if (type == null) return null;

        return switch (type) {
            case "website" -> org.cyclonedx.model.ExternalReference.Type.WEBSITE;
            case "vcs" -> org.cyclonedx.model.ExternalReference.Type.VCS;
            case "issue-tracker" -> org.cyclonedx.model.ExternalReference.Type.ISSUE_TRACKER;
            case "mailing-list" -> org.cyclonedx.model.ExternalReference.Type.MAILING_LIST;
            case "chat" -> org.cyclonedx.model.ExternalReference.Type.CHAT;
            case "documentation" -> org.cyclonedx.model.ExternalReference.Type.DOCUMENTATION;
            case "support" -> org.cyclonedx.model.ExternalReference.Type.SUPPORT;
            case "distribution" -> org.cyclonedx.model.ExternalReference.Type.DISTRIBUTION;
            default -> org.cyclonedx.model.ExternalReference.Type.OTHER;
        };
    }
}
