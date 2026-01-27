package poly.edu.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.service.PdfService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Service
public class PdfServiceImpl implements PdfService {

    // --- 1. XỬ LÝ KHỔ GIẤY ---
    private Rectangle getPageSize(String paperSize) {
        if (paperSize == null) return PageSize.A4;
        switch (paperSize.toUpperCase()) {
            case "A5": return PageSize.A5;
            case "A6": return PageSize.A6;
            case "A7": return PageSize.A7;
            default: return PageSize.A4;
        }
    }

    // --- 2. HÀM IN 1 ĐƠN (GỌI TỪ CONTROLLER) ---
    @Override
    public byte[] generateInvoice(Order order, String paperSize) {
        return createPdfDocument(List.of(order), paperSize);
    }

    // --- [MỚI] 3. HÀM IN NHIỀU ĐƠN (BULK PRINT) ---
    public byte[] generateBulkInvoices(List<Order> orders, String paperSize) {
        return createPdfDocument(orders, paperSize);
    }

    // --- 4. LOGIC TẠO FILE PDF CHUNG ---
    private byte[] createPdfDocument(List<Order> orders, String paperSizeStr) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Rectangle pageSize = getPageSize(paperSizeStr);
            Document document = new Document(pageSize);
            
            // Căn lề nhỏ nếu giấy nhỏ
            if (pageSize == PageSize.A6 || pageSize == PageSize.A7) {
                document.setMargins(10, 10, 10, 10);
            } else {
                document.setMargins(30, 30, 30, 30);
            }

            PdfWriter.getInstance(document, out);
            document.open();

            // Load Font
            ClassPathResource fontResource = new ClassPathResource("fonts/arial.ttf");
            if (!fontResource.exists()) throw new RuntimeException("Lỗi: Không tìm thấy file fonts/arial.ttf");
            
            BaseFont bf = BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 
                    BaseFont.CACHED, fontResource.getContentAsByteArray(), null);

            // Duyệt qua danh sách đơn hàng để vẽ từng trang
            for (int i = 0; i < orders.size(); i++) {
                // Nếu không phải trang đầu thì sang trang mới
                if (i > 0) document.newPage();
                
                // Vẽ nội dung hóa đơn
                drawInvoiceContent(document, orders.get(i), bf, pageSize);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo PDF: " + e.getMessage());
        }
    }

    // --- 5. HÀM VẼ NỘI DUNG (ĐƯỢC TÁCH RA) ---
    private void drawInvoiceContent(Document document, Order order, BaseFont bf, Rectangle pageSize) throws DocumentException {
        // Cỡ chữ
        float titleSize = (pageSize == PageSize.A4) ? 20 : 14;
        float normalSize = (pageSize == PageSize.A4) ? 12 : 9;

        Font fontTitle = new Font(bf, titleSize, Font.BOLD);
        Font fontBold = new Font(bf, normalSize, Font.BOLD);
        Font fontNormal = new Font(bf, normalSize, Font.NORMAL);
        Font fontItalic = new Font(bf, normalSize - 1, Font.ITALIC);

        // Header
        Paragraph shopName = new Paragraph("CỬA HÀNG TRÁI CÂY BAY", fontTitle);
        shopName.setAlignment(Element.ALIGN_CENTER);
        document.add(shopName);

        Paragraph address = new Paragraph("Đ/c: FPT Polytechnic, Q12, TP.HCM", fontNormal);
        address.setAlignment(Element.ALIGN_CENTER);
        address.setSpacingAfter(10);
        document.add(address);

        Paragraph title = new Paragraph("HÓA ĐƠN GIAO HÀNG", new Font(bf, titleSize - 2, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBorder(Rectangle.NO_BORDER);
        cellLeft.addElement(new Paragraph("Mã đơn: " + (order.getOrderCode() != null ? order.getOrderCode() : "#"+order.getId()), fontBold));
        cellLeft.addElement(new Paragraph("Ngày: " + (order.getCreateDate() != null ? sdf.format(order.getCreateDate()) : ""), fontNormal));
        cellLeft.addElement(new Paragraph("Khách: " + (order.getRecipientName() != null ? order.getRecipientName() : order.getAccount().getFullname()), fontNormal));
        infoTable.addCell(cellLeft);

        PdfPCell cellRight = new PdfPCell();
        cellRight.setBorder(Rectangle.NO_BORDER);
        cellRight.addElement(new Paragraph("SĐT: " + (order.getRecipientPhone() != null ? order.getRecipientPhone() : order.getAccount().getPhone()), fontNormal));
        String payment = "Tiền mặt (COD)";
        if (order.getPaymentMethod() != null && order.getPaymentMethod().contains("BANK")) payment = "Chuyển khoản";
        cellRight.addElement(new Paragraph("TT: " + payment, fontBold));
        infoTable.addCell(cellRight);
        document.add(infoTable);

        Paragraph addr = new Paragraph("Địa Chỉ Giao: " + order.getShippingAddress(), fontNormal);
        addr.setSpacingAfter(10);
        document.add(addr);

        // Product Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2.5f, 1, 2.5f});
        
        addTableHeader(table, fontBold, "Tên", "Giá", "SL", "Tiền");
        
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                addCell(table, fontNormal, d.getProduct().getName());
                addCell(table, fontNormal, currencyVN.format(d.getPrice()));
                addCell(table, fontNormal, String.valueOf(d.getQuantity()));
                addCell(table, fontNormal, currencyVN.format(d.getPrice().multiply(d.getQuantity())));
            }
        }
        document.add(table);

        // Total
        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        Paragraph pTotal = new Paragraph("Tổng cộng: " + currencyVN.format(total), fontBold);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        pTotal.setSpacingBefore(5);
        document.add(pTotal);

        // QR Code
        document.add(new Paragraph("\n"));
        try {
            String qrContent = "DH:" + order.getId();
            Image qr = generateQrCodeImage(qrContent);
            qr.setAlignment(Element.ALIGN_CENTER);
            float size = (pageSize == PageSize.A4) ? 100 : 70;
            qr.scaleAbsolute(size, size);
            document.add(qr);
        } catch (Exception e) { }

        Paragraph footer = new Paragraph("Quét mã để tra cứu đơn hàng\r\n"
        		+ "Cảm ơn quý khách đã mua hàng!", fontItalic);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // Helper: Header Bảng
    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, font));
            headerCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            headerCell.setPadding(3);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
        }
    }

    // Helper: Cell Bảng
    private void addCell(PdfPTable table, Font font, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        table.addCell(cell);
    }

    // Helper: QR
    private Image generateQrCodeImage(String text) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return Image.getInstance(pngOutputStream.toByteArray());
    }
}