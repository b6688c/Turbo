package cc.allio.turbo.modules.system.service.impl;

import cc.allio.turbo.extension.oss.*;
import cc.allio.turbo.extension.oss.request.OssGetRequest;
import cc.allio.turbo.extension.oss.request.OssPutRequest;
import cc.allio.turbo.modules.system.properties.FileProperties;
import cc.allio.turbo.common.exception.BizException;
import cc.allio.turbo.common.i18n.ExceptionCodes;
import cc.allio.turbo.common.db.mybatis.service.impl.TurboCrudServiceImpl;
import cc.allio.turbo.common.util.InetUtil;
import cc.allio.turbo.modules.system.entity.SysAttachment;
import cc.allio.turbo.modules.system.mapper.SysAttachmentMapper;
import cc.allio.turbo.modules.system.service.ISysAttachmentService;
import cc.allio.uno.core.StringPool;
import cc.allio.uno.core.util.DateUtil;
import cc.allio.uno.core.util.IoUtils;
import cc.allio.uno.core.util.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 参考自 https://gitee.com/Analyzer/vip-file-upload?_from=gitee_search
 *
 * @author j.x
 * @date 2023/11/18 18:03
 * @since 0.1.0
 */
@Slf4j
@Service
public class SysAttachmentServiceImpl extends TurboCrudServiceImpl<SysAttachmentMapper, SysAttachment> implements ISysAttachmentService {

    private final FileProperties fileProperties;

    public SysAttachmentServiceImpl(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public SysAttachment upload(HttpServletRequest request, MultipartFile file) throws BizException {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new BizException(ExceptionCodes.FILENAME_EMPTY);
        }

        // check file size
        FileProperties.Upload upload = fileProperties.getUpload();

        if (file.getSize() > upload.getMaxSize()) {
            throw new BizException(ExceptionCodes.ATTACHMENT_UPLOAD_EXCEED_SIZE);
        }

        OssExecutor ossExecutor = OssExecutorFactory.getCurrent();

        if (ossExecutor == null) {
            throw new BizException(ExceptionCodes.ATTACHMENT_UPLOAD_EXECUTOR_NOTFOUND);
        }
        InputStream fileInputStream;
        try {
            fileInputStream = file.getInputStream();
        } catch (IOException e) {
            throw new BizException(ExceptionCodes.ATTACHMENT_UPLOAD_EXECUTOR_NOTFOUND);
        }
        // /turbo/2024/06/22/test.txt
        String filepath = DateUtil.getNowPart("/yyyy/MM/dd") + StringPool.SLASH + originalFilename;
        OssPutRequest ossPutRequest =
                OssPutRequest.builder()
                        .inputStream(fileInputStream)
                        .object(filepath)
                        .build();

        boolean hasUpload;
        try {
            hasUpload = ossExecutor.upload(ossPutRequest);
        } catch (Throwable ex) {
            throw new BizException(ExceptionCodes.ATTACHMENT_UPLOAD_EXECUTOR_NOTFOUND);
        }
        // 未上传成功
        if (!hasUpload) {
            throw new BizException(ExceptionCodes.ATTACHMENT_UPLOAD_EXECUTOR_NOTFOUND);
        }

        long filesize = file.getSize();
        String filetype = originalFilename.substring(originalFilename.lastIndexOf(StringPool.ORIGIN_DOT) + 1);
        SysAttachment sysAttachment = new SysAttachment();
        sysAttachment.setFilename(originalFilename);
        sysAttachment.setFilepath(filepath);
        sysAttachment.setProvider(ossExecutor.getProvider());
        sysAttachment.setFilesize(filesize);
        sysAttachment.setFiletype(filetype);
        save(sysAttachment);
        return sysAttachment;
    }

    @Override
    public void download(String path, HttpServletRequest request, HttpServletResponse response) throws BizException {
        OssExecutor ossExecutor = OssExecutorFactory.getCurrent();
        OssGetRequest ossGetRequest = OssGetRequest.builder().object(path).build();
        try {
            OssResponse ossResponse = ossExecutor.download(ossGetRequest);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + convertToFileName(request, ossResponse.getObject()));
            response.setContentType("application/force-download");
            response.setCharacterEncoding("UTF-8");
            StreamUtils.copy(ossResponse.getInputStream(), response.getOutputStream());
        } catch (Throwable ex) {
            throw new BizException(ExceptionCodes.ATTACHMENT_DOWNLOAD_ERROR);
        }
    }

    /**
     * 转换为文件名
     */
    public static String convertToFileName(HttpServletRequest request, String fileName) {
        // 针对IE或者以IE为内核的浏览器：
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        } else {
            // 非IE浏览器的处理
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        }
        return fileName;
    }
}
