package com.ming.shopping.beauty.service.service.impl;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ming.shopping.beauty.service.service.QRCodeService;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author CJ
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {
    @Override
    public BufferedImage generateQRCode(String url) throws IOException, WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 700, 700);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    @Override
    public String scanImage(BufferedImage image) throws IOException {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap).getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
