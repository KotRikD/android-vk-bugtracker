package ru.kotrik.bugtracker.Adapters;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import ru.kotrik.bugtracker.Authdata;
import ru.kotrik.bugtracker.CustomClasses.PathGetter;
import ru.kotrik.bugtracker.Helpers.Async.CookedAttachPostDocument;
import ru.kotrik.bugtracker.Helpers.Async.CookedDocument;
import ru.kotrik.bugtracker.Helpers.Callback;
import ru.kotrik.bugtracker.Helpers.Exceptions.AccessDeniedBtException;
import ru.kotrik.bugtracker.Helpers.Exceptions.NoInternetException;
import ru.kotrik.bugtracker.Helpers.Utils;
import ru.kotrik.bugtracker.Models.Attach;
import ru.kotrik.bugtracker.R;

import static android.app.Activity.RESULT_OK;

public class AddAttachAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_ADD_ICON = 0;
    private Context ctx;

    List<Attach> attachments = new ArrayList<Attach>();

    public AddAttachAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void addAttach(final File file) {
        //GET SERVER
        final ProgressDialog dialog = ProgressDialog.show(((AppCompatActivity) ctx), "",
                ctx.getResources().getString(R.string.wait_plz), true);
        dialog.show();
        new CookedDocument(ctx, new Callback(){
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                onExecuteError(e);
                dialog.dismiss();
            }
            @Override
            public void onSuccess(Document d) {
                try {
                    //GET UPLOAD_URL
                    JSONObject resultJson = new JSONObject(d.body().text());
                    String server = ((JSONObject) resultJson.get("response")).getString("upload_url");

                    //UPLOAD FILE
                    CookedAttachPostDocument post_document = new CookedAttachPostDocument(ctx, new Callback() {
                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            onExecuteError(e);
                            dialog.dismiss();
                        }

                        @Override
                        public void onSuccess(Document d) {
                            try {
                                final JSONObject resultJson = new JSONObject(d.body().text());
                                String file_str = resultJson.getString("file");

                                //SAVE FILE
                                new CookedDocument(ctx, new Callback() {
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                        onExecuteError(e);
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onSuccess(Document d) {
                                        super.onSuccess(d);
                                        try {
                                            JSONObject resultJson = new JSONObject(d.body().text());
                                            JSONArray jsonArray = resultJson.getJSONArray("response");

                                            String id = ((JSONObject)jsonArray.get(0)).getString("id");
                                            String owner_id = ((JSONObject)jsonArray.get(0)).getString("owner_id");
                                            String ext = "."+((JSONObject)jsonArray.get(0)).getString("ext");

                                            attachments.add(new Attach(ext, "doc,"+owner_id+"_"+id));
                                            notifyDataSetChanged();
                                            dialog.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            onExecuteError(e);
                                            dialog.dismiss();
                                        }
                                    }
                                }, "https://api.vk.com/method/docs.save?v=5.78&file="+file_str+"&name="+file.getName()+"&access_token="+Authdata.getTokenVk(ctx)).execute();

                            } catch (Exception e) {
                                e.printStackTrace();
                                onExecuteError(e);
                                dialog.dismiss();
                            }
                        }
                    }, server);
                    post_document.doc.data("file", file.getName(), new FileInputStream(file));
                    post_document.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    onExecuteError(e);
                    dialog.dismiss();
                }
            }
        }, "https://api.vk.com/method/docs.getUploadServer?v=5.78&access_token=" + Authdata.getTokenVk(ctx)).execute("");
    }

    public List<String> getAttaches() {
        List<String> result = new ArrayList<>();
        for(Attach e : attachments) {
            result.add(e.endName);
        }
        return result;
    }

    private void onExecuteError(final Exception e) {
        ((AppCompatActivity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(e instanceof AccessDeniedBtException) {
                    Toast.makeText(ctx, "Нету доступа к Баг-трекеру", Toast.LENGTH_SHORT).show();
                } else if (e instanceof NoInternetException) {
                    Toast.makeText(ctx, "Произошла ошибка соединения", Toast.LENGTH_SHORT).show();
                } else if (e instanceof JSONException) {
                    Toast.makeText(ctx, "Произошла ошибка при парсинге JSON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Произошла неизвестная ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void addFileChoser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            ((AppCompatActivity)ctx).startActivityForResult(Intent.createChooser(intent, "Выберите файл для загрузки"), 0);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctx, "Пожалуйста установите файловый менеджер", Toast.LENGTH_SHORT).show();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = PathGetter.getPath(ctx, uri);

                    System.out.println(path);
                    // Get the file instance
                    File file = new File(path);
                    addAttach(file);
                    // Initiate the upload
                }
                break;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_ADD_ICON) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_report_attach_item_add, parent, false);
            return new ServiceViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_new_report_attach_item, parent, false);
            return new DocumentViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DocumentViewHolder) {
            ((DocumentViewHolder) holder).ext.setText(attachments.get(position-1).type);
        }
    }

    @Override
    public int getItemCount() {
        return attachments.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case TYPE_ADD_ICON:
                return TYPE_ADD_ICON;
            default:
                return 1;
        }
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ServiceViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            addFileChoser();
        }
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView ext;

        public DocumentViewHolder(View itemView) {
            super(itemView);
            ext = itemView.findViewById(R.id.txt_file_type);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((AppCompatActivity) ctx));
            builder.setTitle("Подтверждение")
                    .setMessage("Вы точно хотите удалить этот документ?")
                    .setCancelable(false)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            attachments.remove(getAdapterPosition()-1);
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Нет",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

}
