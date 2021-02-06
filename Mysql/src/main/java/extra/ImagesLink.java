package extra;

public final class ImagesLink {
    public static String icon =
            ImagesLink.class.getResource("/image/applicationIcon.png").toExternalForm();
    public static String foreignKeyIcon =
            ImagesLink.class.getResource("/image/foreignKey.png").toExternalForm();
    public static String primaryKeyIcon =
            ImagesLink.class.getResource("/image/primaryKey.png").toExternalForm();
    public static String uniqueKeyIcon =
            ImagesLink.class.getResource("/image/uniqueKey.png").toExternalForm();

    private ImagesLink() {

    }
}
