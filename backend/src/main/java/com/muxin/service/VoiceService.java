package com.muxin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.apache.commons.io.FileUtils;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VoiceService {
    private static final Logger logger = LoggerFactory.getLogger(VoiceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vosk.enabled:true}")
    private boolean enabled;

    @Value("${vosk.model-path:./models/vosk-model-small-cn-0.22}")
    private String modelPath;

    private Model model;

    @PostConstruct
    public void init() {
        logger.info("Vosk 语音服务状态: enabled={}", enabled);
        if (enabled) {
            try {
                logger.info("当前工作目录: {}", System.getProperty("user.dir"));
                logger.info("正在加载 Vosk 离线语音模型: {}", modelPath);
                
                Path path = Paths.get(modelPath);
                logger.info("模型绝对路径: {}", path.toAbsolutePath());
                
                if (!Files.exists(path)) {
                    logger.error("Vosk 模型不存在: {}", path.toAbsolutePath());
                    return;
                }
                
                if (!Files.isDirectory(path)) {
                    logger.error("Vosk 模型路径不是一个目录: {}", path.toAbsolutePath());
                    return;
                }

                // 检查必要的模型文件
                File amDir = new File(path.toFile(), "am");
                if (!amDir.exists() || !amDir.isDirectory()) {
                    logger.warn("警告: 模型目录中似乎缺少 'am' 子目录，模型可能不完整");
                }

                long startTime = System.currentTimeMillis();
                this.model = new Model(modelPath);
                long duration = System.currentTimeMillis() - startTime;
                
                logger.info("Vosk 离线语音模型加载成功，耗时 {} ms", duration);
            } catch (Exception e) {
                logger.error("加载 Vosk 模型时发生异常: " + e.getMessage(), e);
            }
        }
    }

    public String recognize(byte[] audioData) {
        logger.info("收到语音识别请求: enabled={}, modelReady={}", enabled, (model != null));
        if (!enabled || model == null) {
            return "语音识别服务未启用或模型加载失败";
        }

        String tempId = UUID.randomUUID().toString();
        Path tempDir = Paths.get("temp/voice");
        Path inputPath = tempDir.resolve(tempId + "_input");
        Path outputPath = tempDir.resolve(tempId + ".raw");

        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // 1. 保存原始音频
            Files.write(inputPath, audioData);

            // 2. 使用 FFmpeg 转换为 Vosk 要求的格式: 16000Hz, Mono, 16-bit PCM (s16le)
            logger.info("开始音频格式转换...");
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", inputPath.toString(),
                "-ar", "16000", "-ac", "1", "-f", "s16le", outputPath.toString()
            );
            Process process = pb.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("音频转换超时");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("FFmpeg 转换失败，错误代码: " + process.exitValue());
            }

            // 3. 使用 Vosk 进行识别
            logger.info("开始离线语音识别...");
            try (InputStream is = new FileInputStream(outputPath.toFile());
                 Recognizer recognizer = new Recognizer(model, 16000)) {
                
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    recognizer.acceptWaveForm(buffer, len);
                }
                
                String resultJson = recognizer.getFinalResult();
                JsonNode root = objectMapper.readTree(resultJson);
                String text = root.path("text").asText();
                
                // 去除中文识别结果中的空格
                if (text != null) {
                    text = text.replaceAll("\\s+", "");
                }
                
                logger.info("识别结果 (已处理): {}", text);
                return (text == null || text.isEmpty()) ? "（未能识别出语音内容）" : text;
            }

        } catch (Exception e) {
            logger.error("离线语音识别失败", e);
            return "识别出错: " + e.getMessage();
        } finally {
            // 清理临时文件
            try {
                Files.deleteIfExists(inputPath);
                Files.deleteIfExists(outputPath);
            } catch (IOException e) {
                logger.warn("清理临时文件失败: {}", e.getMessage());
            }
        }
    }
}
