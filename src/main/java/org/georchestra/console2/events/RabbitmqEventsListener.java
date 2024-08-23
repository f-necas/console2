package org.georchestra.console2.events;

import lombok.Setter;
import org.georchestra.console2.mailservice.EmailFactory;
import org.georchestra.console2.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.georchestra.console2.model.AdminLogType;
import org.springframework.beans.factory.annotation.Autowired;

//import jakarta.mail.MessagingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class RabbitmqEventsListener implements MessageListener {

    private @Autowired LogUtils logUtils;

    private @Autowired RoleDao roleDao;

    private @Autowired AccountDao accountDao;

    private @Autowired EmailFactory emailFactory;

    private @Autowired RabbitmqEventsSender rabbitmqEventsSender;

    private static final Set<String> synReceivedMessageUid = Collections.synchronizedSet(new HashSet<>());

    public void onMessage(Message message) {
        String messageBody = new String(message.getBody());
        JSONObject jsonObj = new JSONObject(messageBody);
        String uid = jsonObj.getString("uid");
        String subject = jsonObj.getString("subject");

        if (subject.equals("OAUTH2-ACCOUNT-CREATION") && synReceivedMessageUid.stream().noneMatch(s -> s.equals(uid))) {
            try {
                String fullName = jsonObj.getString("fullName");
                String localUid = jsonObj.getString("localUid");
                String email = jsonObj.getString("email");
                String providerName = jsonObj.getString("providerName");
                String providerUid = jsonObj.getString("providerUid");
                String organization = null;
                if (jsonObj.has("organization")) {
                    organization = jsonObj.getString("organization");
                }
                List<String> superUserAdmins = this.roleDao.findByCommonName("SUPERUSER").getUserList().stream()
                        .map(user -> {
                            try {
                                return this.accountDao.findByUID(user).getEmail();
                            } catch (DataServiceException e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toList());

                this.emailFactory.sendNewOAuth2AccountNotificationEmail(superUserAdmins, fullName, localUid, email,
                        providerName, providerUid, organization, true);

                synReceivedMessageUid.add(uid);
                logUtils.createOAuth2Log(email, AdminLogType.OAUTH2_USER_CREATED, null);
                rabbitmqEventsSender.sendAcknowledgementMessageToGateway(
                        "new OAuth2 account creation notification for " + email + " has been received by console");
            } catch (DataServiceException e) {
                throw new RuntimeException(e);
//            } catch (MessagingException e) {
//                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
