package com.papco.sundar.papcortgs.mail;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.TextFunctions;
import com.papco.sundar.papcortgs.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailTask extends AsyncTask<List<Transaction>,List<Transaction>,List<Transaction>> {

    String FROM_ADDRESS;
    String TO_ADDRESS;
    String SUBJECT;
    String message;
    String USER_ID;


    Context context;
    EmailCallBack callBack;
    GoogleAccountCredential credential;
    Gmail service;
    GoogleSignInAccount account;


    public EmailTask(Context context,EmailCallBack callBack){
        this.context=context;
        this.callBack=callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //Create the credentials and Gmail service
        account=GoogleSignIn.getLastSignedInAccount(context);
        String[] SCOPES={GmailScopes.GMAIL_SEND};
        JacksonFactory jacksonFactory=new JacksonFactory();
        NetHttpTransport httpTransport=new NetHttpTransport();
        GoogleAccountCredential credentials=GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES));
        credentials.setBackOff(new ExponentialBackOff());
        credentials.setSelectedAccountName(account.getEmail());
        Gmail.Builder builder=new Gmail.Builder(httpTransport,jacksonFactory,credentials);
        builder.setApplicationName(context.getResources().getString(R.string.app_name));
        service=builder.build();

        /*if(account.getDisplayName().equals("null"))
            FROM_ADDRESS=account.getEmail();
        else
            FROM_ADDRESS=account.getDisplayName() +" <"+account.getEmail()+">";*/

        FROM_ADDRESS="Papco Offset Private Limited" +" <"+account.getEmail()+">";
        SUBJECT="RTGS Payment intimation from PAPCO";
        USER_ID=GoogleSignIn.getLastSignedInAccount(context).getId();
        if(callBack!=null)
            callBack.onStartSending();

    }

    @Override
    protected List<Transaction> doInBackground(List<Transaction>... lists) {

        List<Transaction> emailList=lists[0];

        // Set all status to queued and send update to mail thread
        for(Transaction trans: emailList){
            trans.emailStatus=EmailService.STATUS_QUEUED;
        }

        /////////////////////////////////////

        //Start sending mails
        for(Transaction trans:emailList){

            trans.emailStatus=EmailService.STATUS_SENDING;
            publishProgress(emailList);

            if(trans.receiver.email==null)
                trans.receiver.email="";

            if(trans.receiver.email.equals("")){
                trans.emailStatus=EmailService.STATUS_FAILED;
                continue;
            }


            TO_ADDRESS=trans.receiver.email;
            message=composeMessage(trans);
            try {
                MimeMessage emailmsg = createEmail(TO_ADDRESS, FROM_ADDRESS, SUBJECT, message);
                sendMessage(service,USER_ID,emailmsg);
                trans.emailStatus=EmailService.STATUS_SENT;
            }catch (Exception e){
                //e.printStackTrace();
                trans.emailStatus=EmailService.STATUS_FAILED;
            }
            /*try {
                Thread.sleep(2000l);
                trans.emailStatus=EmailService.STATUS_SENT;
            }catch (Exception e){

            }*/

        }

        publishProgress(emailList);


        return emailList;
    }

    @Override
    protected void onProgressUpdate(List<Transaction>... values) {
        if(callBack!=null)
            callBack.onUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(List<Transaction> transactions) {
       if(callBack!=null)
           callBack.onComplete(transactions);
    }

    private  MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);


        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
        //return null;
    }

    private  Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private Message sendMessage(Gmail service,
                                      String userId,
                                      MimeMessage emailContent)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
        return message;
    }

    private String composeMessage(Transaction trans){

        String format=context.getString(R.string.email_template);
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NAME,trans.receiver.name);
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NUMBER,trans.receiver.accountNumber);
        format = format.replaceAll(TextFunctions.TAG_AMOUNT,Transaction.amountAsString(trans.amount));
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_BANK,trans.receiver.bank);
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_IFSC,trans.receiver.ifsc);
        format = format.replaceAll(TextFunctions.TAG_SENDER_NAME,trans.sender.name);
        return format;


    }
}
