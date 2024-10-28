package com.papco.sundar.papcortgs.ui.backup

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.dropbox.DropBoxAccount
import com.papco.sundar.papcortgs.ui.backup.BackupScreenState.Dialog
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.OptionsMenu
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.ConfirmationDialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun BackupScreen(
    screenState: BackupScreenState,
    onLink: () -> Unit,
    onUnlink: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onBackPressed: () -> Unit
) {

    val context = LocalContext.current
    val menu = remember {
        prepareMenu(context)
    }

    Scaffold(topBar = {
        RTGSAppBar(
            title = stringResource(id = R.string.dropbox_backup),
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            optionsMenu = {
                OptionsMenu(menuItems = menu) {
                    if(it== context.getString(R.string.sign_out))
                        onUnlink()
                }
            }
        )
    }) {
        if (screenState.isDropBoxConnected)
            DropBoxLinkedScreen(
                modifier = Modifier.padding(it),
                account = screenState.account,
                onDownload = { screenState.showRestoreConfirmationDialog() },
                onBackup = onBackup
        )
        else
            DropBoxUnLinkedScreen(
                modifier = Modifier.padding(it), onLink = onLink
            )
    }

    when(screenState.dialog){
        null ->{}

        is Dialog.RestoreConfirmation->{
            ConfirmationDialog(
                title = stringResource(R.string.restore_dialog_title),
                message = stringResource(id = R.string.restore_dialog_msg),
                positiveButtonText = stringResource(id = R.string.restore),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = {
                    screenState.hideDialog()
                    onRestore()
                },
                onNegativeClick = { screenState.hideDialog() }
            )
        }

        is Dialog.BackupStatus->{
            BackupProgressDialog(
                progress = (screenState.dialog as Dialog.BackupStatus).progress
            )
        }
    }

}

@Composable
private fun DropBoxLinkedScreen(
    account:DropBoxAccount?,
    onDownload: () -> Unit,
    onBackup: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        account?.let{
            DropBoxLoginInfo(userName = it.userName, email = it.email)
        }
        Spacer(modifier = Modifier.height(40.dp))
        Icon(
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(1f),
            painter = painterResource(id = R.drawable.ic_dropbox),
            contentDescription = "DropBox Icon",
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(28.dp))
        BackupToDropBoxButton (onClick = onBackup)
        Spacer(modifier = Modifier.height(28.dp))
        RestoreFromDropBoxButton(onClick = onDownload)
        
    }
}

@Composable
private fun ConnectToDropBoxButton(
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
            Text(text = stringResource(id = R.string.connect_to_drop_box),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun BackupToDropBoxButton(
    onClick:()->Unit
){
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
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
            modifier= Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 18.dp)
        ){
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    painter = painterResource(
                        id = R.drawable.ic_cloud_upload),
                    contentDescription = "Backup to DropBox",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    modifier=Modifier.weight(1f),
                    text = stringResource(id = R.string.backup_to_drop_box),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun RestoreFromDropBoxButton(
    onClick:()->Unit
){
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
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
            modifier= Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 18.dp)
        ){
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    painter = painterResource(
                        id = R.drawable.ic_cloud_download),
                    contentDescription = "Backup to DropBox",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    modifier=Modifier.weight(1f),
                    text = stringResource(id = R.string.restore_from_dropBox),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun BackupProgressDialog(
    progress: String
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small
                )
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = progress, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}


@Composable
private fun DropBoxUnLinkedScreen(
    onLink: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
       Column(
           modifier=Modifier.fillMaxWidth(),
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
            Spacer(modifier = Modifier.height(50.dp))
           Icon(
               modifier= Modifier
                   .width(150.dp)
                   .aspectRatio(1f),
               painter=painterResource(id = R.drawable.ic_connect_dropbox),
               tint = MaterialTheme.colorScheme.secondary,
               contentDescription = "Drop Box Icon"
           )
           Spacer(modifier = Modifier.height(28.dp))
           ConnectToDropBoxButton(onClick = onLink)
       }
    }
}

@Composable
private fun DropBoxLoginInfo(
    userName:String,
    email:String
){
    Surface(
        color = MaterialTheme.colorScheme.secondary
    ) {
        Row(
            modifier= Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(18.dp)
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                imageVector = Icons.Outlined.AccountCircle ,
                contentDescription ="Account Icon"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier=Modifier.weight(1f)
            ) {
                Text(text = userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = email, style = MaterialTheme.typography.titleSmall)
            }

        }
    }
}

private fun prepareMenu(context: Context):List<MenuAction>{
    return listOf(
        MenuAction(
            iconId = R.drawable.ic_logout,
            label = context.getString(R.string.sign_out)
        )
    )
}

@Preview
@Composable
private fun PreviewUnLinkedScreen(){
    PreviewGround {
        DropBoxUnLinkedScreen(onLink = { /*TODO*/ })
    }
}

@Preview
@Composable
private fun PreviewLinkedScreen(){

    val state = remember {
        BackupScreenState().apply {
            isDropBoxConnected=true
            account = DropBoxAccount(
                "Papco Offset Private Limited",
                "papcopvtltd@email.com"
            )
        }
    }

    PreviewGround {
        DropBoxLinkedScreen(
            account = state.account,
            onDownload = {  },
            onBackup = {  })
    }
}

@Composable
fun PreviewGround(
    content: @Composable ()->Unit
){
    RTGSTheme {
        Box(
            modifier= Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ){
            content()
        }
    }
}