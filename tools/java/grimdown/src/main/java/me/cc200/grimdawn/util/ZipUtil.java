package me.cc200.grimdawn.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩包工具
 */
@Slf4j
public abstract class ZipUtil {

    public static final int BUFFER = 1 << 12;

    /**
     * 压缩指定文件夹并打包
     * @param zipFile
     * @param srcDir
     * @return
     */
    public static File zipDir(File zipFile, File srcDir) {
        return zip(zipFile, srcDir.listFiles());
    }

    /**
     * 压缩多个文件并打包
     * @param zipFile
     * @param srcFiles
     * @return
     */
    public static File zip(File zipFile, File... srcFiles) {
        return zip(zipFile, null, null, srcFiles);
    }

    /**
     * 压缩多个文件并打包
     * @param zipFile
     * @param charset
     * @param baseDir
     * @param srcFiles
     * @return
     */
    @SneakyThrows
    public static File zip(File zipFile, Charset charset, String baseDir, File... srcFiles) {
        Objects.requireNonNull(zipFile);
        charset = null == charset ? Charset.defaultCharset() : charset;
        baseDir = null == baseDir ? "" : baseDir;
        String outDir = !"".equals(baseDir) && !baseDir.endsWith("/") ? (baseDir + "/") : baseDir;
//        log.debug(outDir);
        if (!zipFile.exists()) {
            zipFile.createNewFile();
        }
        if (null == srcFiles || srcFiles.length == 0) {
            return zipFile;
        }
        @Cleanup ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile), charset);
        Arrays.stream(srcFiles).forEach(f -> compress(f, zos, outDir));
        zos.flush();
        return zipFile;
    }

    /**
     * 压缩（递归）
     * @param file
     * @param zos
     * @param baseDir
     */
    private static void compress(File file, ZipOutputStream zos, String baseDir) {
        Objects.requireNonNull(file);
        if (file.isDirectory()) {
            compressDir(file, zos, baseDir);
        } else {
            compressFile(file, zos, baseDir);
        }
    }

    /**
     * 压缩文件夹
     * @param srcDir
     * @param zos
     * @param baseDir
     */
    private static void compressDir(File srcDir, ZipOutputStream zos, String baseDir) {
        Objects.requireNonNull(srcDir);
        File[] files = srcDir.listFiles();
        if (null == files || files.length == 0) {
            return;
        }
        Arrays.stream(files).forEach(f -> compress(f, zos, baseDir + srcDir.getName() + "/"));
    }

    /**
     * 压缩文件
     * @param srcFile
     * @param zos
     * @param baseDir
     */
    @SneakyThrows
    private static void compressFile(File srcFile, ZipOutputStream zos, String baseDir) {
        Objects.requireNonNull(srcFile);
        if (!srcFile.exists()) {
            return;
        }
        @Cleanup BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
        ZipEntry ze = new ZipEntry(baseDir + srcFile.getName());
//        log.debug("add zipentry -> {} ~ {}", baseDir, ze.getName());
        zos.putNextEntry(ze);
        int count;
        byte[] buffer = new byte[BUFFER];
        while ((count = bis.read(buffer, 0, BUFFER)) != -1) {
            zos.write(buffer, 0, count);
        }
        zos.closeEntry();
    }

    /*@SneakyThrows
    private static String parentDir(File file) {
        Objects.requireNonNull(file);
        File f = file.getCanonicalFile();
        File pf = f.getParentFile();
        if (null == pf) {
            throw new RuntimeException("file[" + f.getAbsolutePath() + "] has no parent!");
        }
        return pf.getCanonicalPath();
    }*/

    /**
     * 解压到指定目录
     * @param zipFile
     * @param outDir
     */
    public static void unzip(File zipFile, File outDir) {
        unzip(zipFile, outDir, null);
    }

    /**
     * 解压到指定目录
     * @param zipFile
     * @param outDir
     * @param charset
     */
    @SneakyThrows
    public static void unzip(File zipFile, File outDir, Charset charset) {
        charset = null == charset ? Charset.defaultCharset() : charset;
        Objects.requireNonNull(zipFile);
        Objects.requireNonNull(outDir);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        @Cleanup ZipFile zip = new ZipFile(zipFile, charset);
        zip.stream().forEach(e -> {
            File outFile = new File(outDir, e.getName());
            if (e.isDirectory()) {
                outFile.mkdirs();
            } else {
                unzipFile(zip, e, outFile);
            }
        });
    }

    /**
     * 解压压缩包中的单个文件到指定文件
     * @param zipFile
     * @param zipEntry
     * @param outFile
     */
    @SneakyThrows
    private static void unzipFile(ZipFile zipFile, ZipEntry zipEntry, File outFile) {
        Objects.requireNonNull(zipFile);
        Objects.requireNonNull(zipEntry);
        Objects.requireNonNull(outFile);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        @Cleanup InputStream is = zipFile.getInputStream(zipEntry);
        Files.copy(is, Paths.get(outFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
//        log.debug("copy zip file[{}] => {}", zipEntry.getName(), outFile.getAbsolutePath());
    }
}
