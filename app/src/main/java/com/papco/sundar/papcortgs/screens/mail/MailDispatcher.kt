package com.papco.sundar.papcortgs.screens.mail

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.transaction.Transaction.Companion.amountAsString
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailDispatcher(
    private val context: Context
) {

    private val gmailService:Gmail by lazy{ buildGmailService() }

    private val account:GoogleSignInAccount by lazy{
        GoogleSignIn.getLastSignedInAccount(context) ?:
        error(context.getString(R.string.gmail_not_logged_in))
    }

    private val messageFormat = """
        Sir,
        
        This is to inform you that we <SenAccName> has sent an RTGS payment of <TransAmount>/- to your following account 
        
        Account Name: <RecAccName>
        Account Number: <RecAccNumber>
        Bank: <RecBank>
        Amount: <TransAmount>/-
        
        Kindly acknowledge the same for our confirmation purpose.
        
        Thank you
        
        Yours,
        <SenAccName>
        """.trimIndent()

    fun dispatchEmail(transaction: CohesiveTransaction):Boolean {

        return try{
            val email=createEmailMessage(transaction)
            sendEmail(email)
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    private fun buildGmailService():Gmail{

        //Create the credentials and Gmail service
        val account = GoogleSignIn.getLastSignedInAccount(context)
        require(account!=null){
            "Gmail not logged in. Please log in to gmail first before sending emails"
        }
        val scopes = arrayOf(GmailScopes.GMAIL_SEND)
        val jacksonFactory = JacksonFactory()
        val httpTransport = NetHttpTransport()

        val credentials = GoogleAccountCredential.usingOAuth2(context, listOf(*scopes))
        credentials.setBackOff(ExponentialBackOff())
        credentials.setSelectedAccountName(account.email)

        val builder = Gmail.Builder(httpTransport, jacksonFactory, credentials)
        builder.setApplicationName(context.resources.getString(R.string.app_name))
        return builder.build()

    }

    private fun createEmailMessage(transaction: CohesiveTransaction):Message {

        val props = Properties()
        val session = Session.getDefaultInstance(props, null)

        val fromAddress = context.getString(R.string.default_sender_name) + " <${account.email}>"

        val mimeMessage = MimeMessage(session)
        mimeMessage.setFrom(InternetAddress(fromAddress))
        mimeMessage.addRecipient(
            javax.mail.Message.RecipientType.TO, InternetAddress(transaction.receiver.email)
        )
        mimeMessage.subject = context.getString(R.string.default_mail_subject)
        mimeMessage.setText(composeMessageBody(transaction))
        return encryptMimeMessageToEmailMessage(mimeMessage)
    }


    private fun composeMessageBody(trans: CohesiveTransaction): String {

        var format = messageFormat

        format =
            format.replace(TextFunctions.TAG_RECEIVER_ACC_NAME, trans.receiver.name)
        format = format.replace(
            TextFunctions.TAG_RECEIVER_ACC_NUMBER, trans.receiver.accountNumber
        )
        format = format.replace(TextFunctions.TAG_AMOUNT, amountAsString(trans.transaction.amount))
        format = format.replace(TextFunctions.TAG_RECEIVER_BANK, trans.receiver.bank)
        format = format.replace(TextFunctions.TAG_RECEIVER_IFSC, trans.receiver.ifsc)
        format = format.replace(TextFunctions.TAG_SENDER_NAME, trans.sender.name)
        return format
    }

    private fun encryptMimeMessageToEmailMessage(emailContent: MimeMessage): Message {

        val buffer = ByteArrayOutputStream()
        emailContent.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = Base64.encodeBase64URLSafeString(bytes)
        val message = Message()
        message.setRaw(encodedEmail)
        return message
    }

    private fun sendEmail(message: Message){

        gmailService
            .users()
            .messages()
            .send(account.id, message)
            .execute()

    }

}