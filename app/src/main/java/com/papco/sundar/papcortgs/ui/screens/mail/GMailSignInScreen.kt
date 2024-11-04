package com.papco.sundar.papcortgs.ui.screens.mail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.GMailUtil
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme


@Composable
fun GmailSignInScreen(
    onConnected: () -> Unit,
    onBackPressed:()->Unit
){

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val completedTask=GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                completedTask.getResult(ApiException::class.java)
                // Signed in successfully, show authenticated UI.
                onConnected()
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
            }
        }
    )

    val context = LocalContext.current
    val gmailUtil = remember{GMailUtil(context)}

    Scaffold(
        topBar = {
            RTGSAppBar(
                title = stringResource(id = R.string.gmail),
                isBackEnabled = true,
                onBackPressed = onBackPressed
            )
        }
    ) {
        GMailSignInScreenContent(
            modifier = Modifier.padding(it),
            onConnect = {
                launcher.launch(gmailUtil.client.signInIntent)
            }
        )
    }
}



@Composable
private fun GMailSignInScreenContent(
    onConnect: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column(
            modifier= Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                modifier= Modifier
                    .width(90.dp)
                    .aspectRatio(1f),
                painter= painterResource(id = R.drawable.ic_gmail),
                contentDescription = "Drop Box Icon"
            )
            Spacer(modifier = Modifier.height(28.dp))
            ConnectToGMailButton(onClick = onConnect)
        }
    }
}

@Composable
private fun ConnectToGMailButton(
    onClick:()->Unit
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(
                1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ){
        Box(
            modifier=Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(text = stringResource(id = R.string.connect_to_gmail),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
private fun PreviewGmailScreen(){
    RTGSTheme {
        GmailSignInScreen(onConnected = { /*TODO*/ }) {

        }
    }
}