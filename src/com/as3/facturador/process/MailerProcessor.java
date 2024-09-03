/*
 * MailerProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.CompaniaDAO;
import com.as3.facturador.dao.SmtpDAO;
import com.as3.facturador.vo.CompaniaVO;
import com.as3.facturador.vo.SmtpVO;
import com.softcoatl.utils.StringUtils;
import com.softcoatl.utils.logging.LogManager;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.as3.facturador.schema.ComprobanteDetisa;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MailerProcessor extends BaseProcessor implements FacturaProcessor {

    private static class SMTPAuthenticator extends javax.mail.Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            String username = smtp.getLoginuser();
            String password = smtp.getLoginpass();
            return new PasswordAuthentication(username, password);
        }
    }

    private static final class Loader {
        static final MailerProcessor INSTANCE = new MailerProcessor();
    }

    public static MailerProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private static SmtpVO smtp;
    private static Properties props;
    private static final CompaniaVO emisor = CompaniaDAO.get();
    private static final String email_regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    private static Pattern email_pattern = Pattern.compile(email_regexp);
  

    private MailerProcessor() {
        smtp = SmtpDAO.getActive();
        props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtp.getServer());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", smtp.getPort());
        props.put("mail.debug", "true");
        if (smtp.isSecure()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "*");
        }
    }

    public void postMail(ComprobanteDetisa comprobante, String subject, String message) throws MessagingException, UnsupportedEncodingException {

        Session session;
        
        if (smtp.isAuth()) {
            session = Session.getDefaultInstance(props, new SMTPAuthenticator());
        } else {
            session = Session.getDefaultInstance(props);
        }

        session.setDebug(true);

        Message mimeMessage = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(smtp.getSender(), "Sistema de Facturación Omicrom");
        mimeMessage.setFrom(addressFrom);

        if (comprobante.getMailerList()!=null) {
            try {
                for (String recipient : comprobante.getMailerList()) {
                    LogManager.debug(recipient);
                    Matcher matcher = email_pattern.matcher(recipient);
                    if (matcher.matches()) {
                        mimeMessage.addRecipients(Message.RecipientType.TO, (Address[]) InternetAddress.parse(recipient));
                    }
                }
            } catch (AddressException ae) {
                LogManager.error("Error agregando copia a " + comprobante.getCliente().getEmailcc());
            }
        } else if (!StringUtils.isNVL(comprobante.getCliente().getEmail())) {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(comprobante.getCliente().getEmail()));

            if (!StringUtils.isNVL(comprobante.getCliente().getEmailcc())) {
                try {
                    for (String cc_addr : comprobante.getCliente().getEmailcc().split(";")) {
                        Matcher matcher = email_pattern.matcher(cc_addr);
                        if (matcher.matches()) {
                            mimeMessage.addRecipients(Message.RecipientType.CC, (Address[]) InternetAddress.parse(cc_addr));
                        }
                    }
                } catch (AddressException ae) {
                    LogManager.error("Error agregando copia a " + comprobante.getCliente().getEmailcc());
                }
            }
        }

        Multipart multipart = new MimeMultipart();

        BodyPart messagePart = new MimeBodyPart();
        String htmlText = "<body><table width=\"800\" border=\"0\"><tr><td colspan=\"3\">" + message + "</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\">&nbsp;</td></tr>"
                + "<tr><td colspan=\"3\" align=\"center\"><strong>*** Este correo se envia de forma autom&aacute;tica, por favor no responda a esta direcci&oacte;n ***</strong></td></tr></table></body>";
        messagePart.setContent(htmlText, "text/html");
        multipart.addBodyPart(messagePart);

        MimeBodyPart pdfPart = new MimeBodyPart();
        pdfPart.setDataHandler(new DataHandler(comprobante.getPdf(), "application/pdf"));
        pdfPart.setFileName(comprobante.getUuid() + ".pdf");
        multipart.addBodyPart(pdfPart);

        MimeBodyPart xmlPart = new MimeBodyPart();
        xmlPart.setDataHandler(new DataHandler(comprobante.getXml(), "application/pdf"));
        xmlPart.setFileName(comprobante.getUuid() + ".xml");
        multipart.addBodyPart(xmlPart);

        mimeMessage.setContent(multipart);
        mimeMessage.setSubject(subject);

        LogManager.info("Enviando CFDI por correo electrónico a " + Arrays.asList(mimeMessage.getAllRecipients()).stream().map(Address::toString).collect(Collectors.joining("|")));
        Transport.send(mimeMessage);
    }

    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        
        LogManager.info("Cargando comprobante versión " + comprobante.getVersion());
        LogManager.debug(comprobante);
        if (comprobante.isMailer() && (comprobante.getCliente()!=null || comprobante.getMailerList()!=null)) {
            String content = "Estimado <b>" + comprobante.getCliente().getRazonSocial() + "</b>:<br /><br />"
                    + "Le estamos enviando por este medio el <b>CFDI Comprobante Fiscal Digital (Factura Electr&oacute;nica) Folio " + comprobante.getFolio() + " con UUID:" + comprobante.getUuid() + "</b>"
                    + " correspondiente a su consumo en <b>" + emisor.getRazonSocial() + "</b>."
                    + "<br /><br />Nos ponemos a sus &oacute;rdenes para cualquier aclaraci&oacute;n al respecto al Tel&eacute;fono " + emisor.getTelefono()
                    + "<br /><br /><br />------------------------------------------------------------------------------------------------------------------------------------------------------------------------<br/>"
                    + "Sistema de Facturaci&oacute;n Electr&oacute;nica / <b>Deti</b> Desarrollo y Transferencia de Inform&aacute;tica S.A. de C.V. / detisa.com.mx";
            String subject = "Envío de Factura Electrónica Folio " + comprobante.getFolio();

            LogManager.info("Enviando CFDI por correo electrónico " + subject);
            LogManager.debug(comprobante.getCliente());
            if (comprobante.getCliente().isSendEmail() || comprobante.isMailer()) {
                new Thread(() -> {
                    try {
                        postMail(comprobante, subject, content);
                    } catch (MessagingException | UnsupportedEncodingException ex) {
                        LogManager.error(ex);
                    }

                }).start();
            }
        }
        return comprobante;
    }
}
