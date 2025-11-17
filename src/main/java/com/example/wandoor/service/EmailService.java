package com.example.wandoor.service;

import com.example.wandoor.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtp(String to, String otp){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Wandoor Internet Banking - Kode OTP Anda");

            String html = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; background:#f4f6fa; padding: 30px;">
                    <div style="max-width: 600px; margin: auto; background:white; border-radius:12px;
                                padding: 30px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                        <!-- HEADER -->
                        <div style="text-align:center; margin-bottom: 20px;">
                            <div style="font-size: 28px; font-weight: 700; color:#0A2A66;">
                                Wandoor
                            </div>
                            <div style="font-size: 14px; color:#6c757d;">
                                Internet Banking Security Verification
                            </div>
                        </div>

                        <p style="font-size: 16px; color:#333;">
                            Halo Nasabah <b>Wandoor</b>,
                        </p>

                        <p style="font-size: 15px; color:#444;">
                            Demi keamanan akun Anda, berikut adalah kode OTP untuk proses login Internet Banking:
                        </p>

                        <!-- OTP BOX -->
                        <div style="margin: 25px 0; text-align:center;">
                            <div style="display:inline-block; padding: 18px 35px; 
                                        background:#eff4ff; border-radius: 10px;
                                        border-left: 6px solid #0A2A66; 
                                        font-size: 32px; font-weight:700; color:#0A2A66; 
                                        letter-spacing: 3px;">
                                %s
                            </div>
                        </div>

                        <p style="font-size: 15px; color:#444;">
                            Kode ini berlaku selama <b>5 menit</b>.  
                            Mohon <span style="color:#d9534f; font-weight:600;">jangan berikan kode OTP</span> ini kepada siapa pun, 
                            termasuk pihak yang mengaku sebagai pegawai Wandoor.
                        </p>

                        <br/>

                        <!-- FOOTER -->
                        <p style="font-size: 15px; color:#333;">Terima kasih telah menggunakan layanan Wandoor.</p>
                        <p style="font-size: 15px; font-weight:600; color:#0A2A66;">Tim Keamanan Digital Wandoor</p>

                        <hr style="border:none; border-top:1px solid #e5e5e5; margin: 30px 0;" />

                        <!-- DISCLAIMER -->
                        <p style="font-size: 12px; color:#777; line-height:1.5;">
                            Email ini dikirim secara otomatis oleh sistem Wandoor Internet Banking.
                            Mohon untuk tidak membalas email ini.  
                            Jika Anda merasa tidak melakukan permintaan OTP, segera hubungi layanan pelanggan Wandoor.
                        </p>
                    </div>
                </div>
                """.formatted(otp);

            helper.setText(html, true);
            mailSender.send(mimeMessage);

        } catch (BusinessException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error while send OTP", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                    "Something went wrong while send OTP", e);
        }
    }
}
