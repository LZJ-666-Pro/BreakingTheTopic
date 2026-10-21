package com.poti.user.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片压缩工具（仅用 JDK 原生 ImageIO，不引入第三方依赖）。
 *
 * 用途：头像、聊天图片上传后压缩，避免把几 MB 的原图直接落盘。
 * 之前出现过 3.38MB 的头像走公网隧道要 28 秒才能加载的情况。
 *
 * 策略：
 * - 原图最长边超过 maxSide 或体积超过 sizeThreshold 才处理，小图原样保留
 * - 等比缩放到最长边 maxSide
 * - 文件名、扩展名保持不变（数据库无需改动）
 * - 无法识别的格式（webp/gif 等 ImageIO 不支持的）直接跳过，不影响上传
 */
@Slf4j
public final class ImageCompressor {

    /** 头像最长边（头像显示尺寸通常 < 200px，512 足够清晰且体积可控） */
    public static final int AVATAR_MAX_SIDE = 512;
    /** 聊天图片最长边 */
    public static final int CHAT_MAX_SIDE = 1280;
    /** 超过该体积才压缩 */
    public static final long SIZE_THRESHOLD = 200 * 1024L;
    /** JPEG 输出质量 */
    private static final float JPEG_QUALITY = 0.85f;

    private ImageCompressor() {
    }

    /**
     * 原地压缩图片文件（覆盖写回同一路径）。
     *
     * @param file       目标文件
     * @param extension  扩展名（含点，如 .jpg，可为空则由文件推断）
     * @param maxSide    最长边上限
     * @return 压缩后的文件大小（未处理则返回原大小）
     */
    public static long compressInPlace(File file, String extension, int maxSide) {
        if (file == null || !file.exists() || file.length() <= 0) {
            return 0L;
        }
        long originalSize = file.length();
        String ext = normalizeExt(extension, file.getName());

        // 小文件且不超大则跳过（省一次解码开销）
        if (originalSize <= SIZE_THRESHOLD) {
            return originalSize;
        }

        BufferedImage source = null;
        try {
            source = ImageIO.read(file);
        } catch (IOException e) {
            log.warn("图片读取失败，跳过压缩: {}, {}", file.getName(), e.getMessage());
            return originalSize;
        }
        if (source == null) {
            // ImageIO 不支持的格式（webp/gif 等），保持原样
            log.info("不支持的图片格式，跳过压缩: {}", file.getName());
            return originalSize;
        }

        int width = source.getWidth();
        int height = source.getHeight();
        boolean hasAlpha = source.getColorModel() != null && source.getColorModel().hasAlpha();
        boolean tooLarge = width > maxSide || height > maxSide;
        if (!tooLarge) {
            return originalSize;
        }

        try {
            BufferedImage scaled = scale(source, maxSide, hasAlpha);
            writeImage(scaled, file, ext);
            long newSize = file.length();
            // 极端情况下（如原图高压缩比）压缩后反而变大，则回滚为原图
            if (newSize >= originalSize) {
                log.info("压缩后体积未减小，保留原图: {} ({} -> {})",
                        file.getName(), originalSize, newSize);
                return originalSize;
            }
            log.info("图片压缩完成: {} {}x{} {}KB -> {}x{} {}KB",
                    file.getName(), width, height, originalSize / 1024,
                    scaled.getWidth(), scaled.getHeight(), newSize / 1024);
            return newSize;
        } catch (Exception e) {
            log.warn("图片压缩失败，保留原图: {}, {}", file.getName(), e.getMessage());
            return originalSize;
        } finally {
            source.flush();
        }
    }

    /**
     * 头像压缩（最长边 512）
     */
    public static long compressAvatar(File file, String extension) {
        return compressInPlace(file, extension, AVATAR_MAX_SIDE);
    }

    /**
     * 聊天图片压缩（最长边 1280）
     */
    public static long compressChatImage(File file, String extension) {
        return compressInPlace(file, extension, CHAT_MAX_SIDE);
    }

    private static String normalizeExt(String extension, String fileName) {
        String ext = extension;
        if (ext == null || ext.trim().isEmpty()) {
            int idx = fileName == null ? -1 : fileName.lastIndexOf('.');
            ext = idx >= 0 ? fileName.substring(idx) : "";
        }
        return ext.toLowerCase();
    }

    private static BufferedImage scale(BufferedImage source, int maxSide, boolean hasAlpha) {
        int width = source.getWidth();
        int height = source.getHeight();
        double ratio = Math.min((double) maxSide / width, (double) maxSide / height);
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));

        int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    private static void writeImage(BufferedImage image, File file, String ext) throws IOException {
        String format;
        if (ext.equals(".jpg") || ext.equals(".jpeg")) {
            format = "jpeg";
        } else if (ext.equals(".png")) {
            format = "png";
        } else {
            format = "jpeg";
        }

        if ("jpeg".equals(format)) {
            writeJpeg(image, file);
        } else {
            // PNG：IMAGEIO 无质量参数，靠缩放到位即可
            if (!ImageIO.write(image, "png", file)) {
                writeJpeg(image, file);
            }
        }
    }

    private static void writeJpeg(BufferedImage image, File file) throws IOException {
        // JPEG 不支持透明通道
        BufferedImage rgb = image;
        if (rgb.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage converted = new BufferedImage(
                    rgb.getWidth(), rgb.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = converted.createGraphics();
            try {
                g.drawImage(rgb, 0, 0, java.awt.Color.WHITE, null);
            } finally {
                g.dispose();
            }
            rgb = converted;
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            ImageIO.write(rgb, "jpeg", file);
            return;
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
        }
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmp);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        // 原子替换
        if (!tmp.renameTo(file)) {
            java.nio.file.Files.move(tmp.toPath(), file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
