package com.papco.sundar.papcortgs;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityComposeMessage extends AppCompatActivity {

    RecyclerView recycler;
    TagAdapter adapter = new TagAdapter();
    EditText messageView;
    TextView sampleMessageView;
    TextWatcher watcher;
    SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);
        pref=getSharedPreferences("mysettings",MODE_PRIVATE);
        recycler = findViewById(R.id.compose_recycler);
        messageView = findViewById(R.id.compose_message_box);
        sampleMessageView = findViewById(R.id.compose_sample_box);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new TagAdapter();
        watcher = new messageWatcher();
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycler.addItemDecoration(new SpacingDecoration(this,SpacingDecoration.HORIZONTAL,16,8,16));
        recycler.setAdapter(adapter);

        getSupportActionBar().setTitle("Compose Message");
        messageView.addTextChangedListener(watcher);
        loadDefaultMessageFormat();
        markBLUEinHeading();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compose,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                return true;

            case R.id.action_compose_done:
                saveMessageTemplate();
                finish();
                return true;

            case R.id.action_compose_reset:
                showResetMessageDialog();
                return true;
        }

        return false;

    }

    private void loadDefaultMessageFormat() {

        if(pref==null)
            return;


        String format=pref.getString("message_template","NULL");
        if(format.equals("NULL"))
           messageView.setText("");
        else
            messageView.setText(format);

    }

    private void markBLUEinHeading(){

        TextView headingText=findViewById(R.id.compose_info);
        String temp=headingText.getText().toString();
        SpannableString result = new SpannableString(temp);
        int startIndex=temp.indexOf("BLUE",0);
        result.setSpan(new ForegroundColorSpan(Color.BLUE),startIndex,startIndex+4,0);
        headingText.setText(result,TextView.BufferType.SPANNABLE);

    }

    private void onRecyclerItemClicked(int adapterPosition) {

        String toInsert = "UNKNOWN";
        switch (adapterPosition) {

            case 0:
                toInsert = TextFunctions.TAG_RECEIVER_ACC_NAME;
                break;

            case 1:
                toInsert = TextFunctions.TAG_RECEIVER_ACC_NUMBER;
                break;

            case 2:
                toInsert = TextFunctions.TAG_AMOUNT;
                break;

            case 3:
                toInsert = TextFunctions.TAG_RECEIVER_BANK;
                break;

            case 4:
                toInsert = TextFunctions.TAG_RECEIVER_IFSC;
                break;

            case 5:
                toInsert = TextFunctions.TAG_SENDER_NAME;
                break;
        }

        messageView.getText().insert(messageView.getSelectionStart(), toInsert + " ");


    }

    private void updateSampleMessage(Editable source) {

        String result = source.toString();
        result = result.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NAME, "ABC PVT LTD");
        result = result.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NUMBER, "123456789");
        result = result.replaceAll(TextFunctions.TAG_AMOUNT, "Rs.12,345");
        result = result.replaceAll(TextFunctions.TAG_RECEIVER_BANK, "ICICI, Svks");
        result = result.replaceAll(TextFunctions.TAG_RECEIVER_IFSC, "ICIC0000012");
        result = result.replaceAll(TextFunctions.TAG_SENDER_NAME, "PAPCO PRIVATE LIMITED");
        sampleMessageView.setText(result);

    }

    private void showResetMessageDialog(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Sure want to reset the text to default format?");
        builder.setNegativeButton("CANCEL",null);
        builder.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                messageView.setText(TextFunctions.getDefaultMessageFormat());
            }
        });
        builder.create().show();
    }

    private void saveMessageTemplate(){

        if(pref==null)
            return;

        String format=messageView.getText().toString();
        pref.edit().putString("message_template",format).commit();

    }

    class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagVH> {


        @NonNull
        @Override
        public TagVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new TagVH(getLayoutInflater().inflate(R.layout.compose_tag_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TagVH holder, int position) {

            holder.bind();

        }

        @Override
        public int getItemCount() {
            return 6;
        }

        class TagVH extends RecyclerView.ViewHolder {

            TextView txtName;

            public TagVH(View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.tag_item_name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onRecyclerItemClicked(getAdapterPosition());
                    }
                });
            }

            public void bind() {

                String name = "UNKNOWN TAG";
                switch (getAdapterPosition()) {

                    case 0:
                        name = "RECEIVER\nACCOUNT\nNAME";
                        break;

                    case 1:
                        name = "RECEIVER\nACCOUNT\nNUMBER";
                        break;

                    case 2:
                        name = "TRANSACTION\nAMOUNT";
                        break;

                    case 3:
                        name = "RECEIVER\nBANK";
                        break;

                    case 4:
                        name = "RECEIVER\nIFSC";
                        break;

                    case 5:
                        name = "SENDER\nACCOUNT\nNAME";
                        break;

                }
                txtName.setText(name);

            }
        }
    }

    class messageWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            TextFunctions.removeAllForegroundSpan(editable);
            TextFunctions.markAllTagsWithSpan(editable);
            updateSampleMessage(editable);


        }
    }

}
