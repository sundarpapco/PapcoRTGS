package com.papco.sundar.papcortgs.screens.transaction.common;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionVM;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM.ReceiverSelectionListener;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.SenderSelectionVM;
import com.papco.sundar.papcortgs.screens.transaction.listTransaction.TransactionListVM;

public class TransactionActivityVM extends AndroidViewModel {

    private TransactionListVM transactionListVM;
    private ReceiverSelectionVM receiverSelectionVM;
    private SenderSelectionVM senderSelectionVM;
    private CreateTransactionVM createTransactionVM;

    public TransactionActivityVM(@NonNull Application application) {
        super(application);
    }

    public TransactionListVM getTransactionListVM(int groupId) {

        if (transactionListVM == null)
            transactionListVM = new TransactionListVM(getApplication(), groupId);

        return transactionListVM;
    }

    public ReceiverSelectionVM getReceiverSelectionVM(boolean needNewInstance, int groupId) {

        if (receiverSelectionVM == null || needNewInstance) {
            receiverSelectionVM = new ReceiverSelectionVM(getApplication(), groupId);
            receiverSelectionVM.setCallback(new ReceiverSelectionListener() {
                @Override
                public void onReceiverSelected(Receiver receiver) {
                    if (createTransactionVM != null)
                        createTransactionVM.setReceiver(receiver);
                }
            });
        }

        return receiverSelectionVM;
    }

    public SenderSelectionVM getSenderSelectionVM(boolean needNewInstance) {

        if (senderSelectionVM == null || needNewInstance) {
            senderSelectionVM = new SenderSelectionVM(getApplication());
            senderSelectionVM.setCallback(new SenderSelectionVM.SenderSelectionListener() {
                @Override
                public void onSenderSelected(Sender sender) {
                    if (createTransactionVM != null)
                        createTransactionVM.setSender(sender);

                }
            });
        }

        return senderSelectionVM;
    }

    public CreateTransactionVM getCreateTransactionVM(boolean needNewInstance, int groupId, int loadTransactionId) {

        if (createTransactionVM == null || needNewInstance)
            createTransactionVM = new CreateTransactionVM(getApplication(), groupId, loadTransactionId);

        return createTransactionVM;
    }


}
