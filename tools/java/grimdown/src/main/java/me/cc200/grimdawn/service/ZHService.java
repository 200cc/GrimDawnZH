package me.cc200.grimdawn.service;


import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.cc200.grimdawn.util.ZipUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 汉化相关
 */
@Slf4j
public class ZHService {

    /**
     * 打包文件夹：汉化
     */
    public void zipZH() {
        String prjDir = getPrjDir();
        File zhDir = new File(prjDir + "汉化");
        assert zhDir.exists() && zhDir.isDirectory();
        String zipFile = getBuildDir() + "ZH_" + System.currentTimeMillis() + ".zip";
        File res = ZipUtil.zip(new File(zipFile), zhDir);
        log.debug("zip {} to {}", zhDir, res.getAbsolutePath());
    }

    /**
     * 汉化美化
     */
    @SneakyThrows
    public void beautify() {
        String prjDir = getPrjDir();
        File zhBak = new File(getBuildDir() + "汉化bak");
        copyDir(new File(prjDir + "汉化"), zhBak);

        Files.walk(zhBak.toPath())
                .filter(p -> p.toString().endsWith("_items.txt"))
                .forEach(p -> {
                    try {
                        List<String> newLines = Files.readAllLines(p).stream()
                                .map(l -> beautifySeqRule(l).beautify())
                                .collect(Collectors.toList());
                        Files.write(p, String.join("\n", newLines).getBytes(Charset.forName("utf8")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        String zipFile = getBuildDir() + "ZH_BEAUTIFY_" + System.currentTimeMillis() + ".zip";
        ZipUtil.zip(new File(zipFile), zhBak);
        delete(zhBak);
    }

    /**
     * 在锻造材料、遗物前添加编号
     * @param line
     * @return
     */
    private static SeqRule beautifySeqRule(String line) {
        if (line.startsWith("tagComp")) {
            return SeqRule.builder().start("tagComp")       .regex("^(tagComp(\\w\\d{3})Name=)(.+)$")       .replace("$1[$2]$3").line(line).build();
        } else if (line.startsWith("tagGDX1Comp")) {
            return SeqRule.builder().start("tagGDX1Comp")   .regex("^(tagGDX1Comp(\\w\\d{3})Name=)(.+)$")   .replace("$1[$2]$3").line(line).build();
        } else if (line.startsWith("tagRelic")) {
            return SeqRule.builder().start("tagRelic")      .regex("^(tagRelic(\\w\\d{3})=)(.+)$")          .replace("$1[$2]$3").line(line).build();
        } else if (line.startsWith("tagGDX1Relic")) {
            return SeqRule.builder().start("tagGDX1Relic")  .regex("^(tagGDX1Relic(\\w\\d{3})=)(.+)$")      .replace("$1[$2]$3").line(line).build();
        } else if (line.startsWith("tagGDX2Relic")) {
            return SeqRule.builder().start("tagGDX2Relic")  .regex("^(tagGDX2Relic(\\w\\d{3})=)(.+)$")      .replace("$1[$2]$3").line(line).build();
        }
        return SeqRule.builder().line(line).build();
    }

    @Builder
    @Data
    static class SeqRule {
        String start;
        String regex;
        String replace;
        String line;
        public String beautify() {
            if (null != start && null != regex && !line.contains("[") && !line.contains("]")
                    && line.startsWith(start) && Pattern.compile(regex).matcher(line).find()) {
                log.debug("line : {}", line);
                String rline = line.replaceAll(regex, replace);
                log.debug("line*: {}", rline);
                return rline;
            }
            return line;
        }
    }

    /**
     * 文件夹备份
     * @param srcDir
     * @param tarDir
     */
    @SneakyThrows
    private void copyDir(File srcDir, File tarDir) {
        assert null != srcDir && srcDir.exists();
        if (srcDir.isDirectory()) {
            if (!tarDir.exists()) {
                tarDir.mkdirs();
            }
            String[] files = srcDir.list();
            if (null == files) {
                return;
            }
            for (String f : files) {
                copyDir(new File(srcDir, f), new File(tarDir, f));
            }
        } else {
            Files.copy(srcDir.toPath(), tarDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.debug("copy {} to {}", srcDir.getAbsolutePath(), tarDir.getAbsolutePath());
        }
    }

    /**
     * 文件夹删除
     * @param file
     */
    private static void delete(File file) {
        if (null == file) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (null != file.listFiles()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                delete(f);
            }
        }
    }

    /**
     * 项目路径
     * @return
     */
    private String getPrjDir() {
        String workDir = getWorkDir();
        return workDir.substring(0, workDir.indexOf("tools\\java\\grimdown"));
    }

    /**
     * 工作目录
     * @return
     */
    private String getWorkDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 编译目录
     * @return
     */
    private String getBuildDir() {
        String buildDir = getWorkDir() + "\\target";
        File f = new File(buildDir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return buildDir + "\\";
    }

}
