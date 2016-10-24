package com.zeone.inspection.ui.pop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zeone.inspection.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印提示弹出框
 * Created by yangsr on 2016/10/13.
 */
public class PrintingPOP extends Dialog {
    //01:维护维修单，02：安全巡查单，03：设备巡检单，04：安全检查整改单，05：设备巡检整改单
    private String[] mlistText = {"维护维修单", "安全检查单", "设备巡检单", "安全检查整改单", "设备巡检整改单"};
    private Boolean[] bl = {false, false, false, false, false, false};
    private String mtitle;
    private Context context;
    private SimpleAdapter adapter;
    ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
    private WindowManager.LayoutParams p;
    private PrintingPOPListener dialogListener;
    /**
     * 打印回调监听接口
     */
    public interface PrintingPOPListener {
        public void onPrintok(String type);
    }

    public PrintingPOP(Context context) {
        super(context);
        this.context = context;
        String title = "选择打印内容";
        mtitle = title.replaceAll(":", "");
    }
    public void setPrintCallbacklistener(PrintingPOPListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.printpop);
        p = getWindow().getAttributes();  //获取对话框当前的参数值

        if (context instanceof Activity) {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getMetrics(dm);
            p.height = (int) (dm.heightPixels * 0.58);   //高度设置为屏幕的0.4
            p.width = (int) (dm.widthPixels * 0.95);    //宽度设置为屏幕的0.6
        } else {
            p.height = 300;
            p.width = 400;
        }
        getWindow().setAttributes(p);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(mtitle);
        ListView listview = (ListView) findViewById(R.id.X_listview);
        int lengh = mlistText.length;
        for (int i = 0; i < lengh; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("text", mlistText[i]);
            mData.add(item);
        }
        adapter = new SetSimpleAdapter(context, mData, R.layout.print_dialoglistitem, new String[]{"text"},
                new int[]{R.id.X_item_text});
        // 给listview加入适配器
        listview.setAdapter(adapter);

        listview.setItemsCanFocus(false);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setOnItemClickListener(new ItemOnClick());
        Button btnOk = (Button) findViewById(R.id.tablet_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintingPOP.this.dismiss();
                adapter.notifyDataSetChanged();
                downLoadFile();
            }
        });
        Button btnCancel = (Button) findViewById(R.id.tablet_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyDataSetChanged();
                clearList();
                cancel();
            }
        });
    }

    //清除操作
    private void clearList() {
        for (int i = 0; i < bl.length; i++) {
            bl[i] = false;
        }
    }

    //打印文件
    /**
     * 01:维护维修单，02：安全巡查单，03：设备巡检单，04：安全检查整改单，05：设备巡检整改单
     */
    private void downLoadFile() {
        for (int i = 0; i < bl.length; i++) {
            if (bl[i] == true) {

                if (i == 0) {
                    if (dialogListener!=null) {
                        dialogListener.onPrintok("01");
                    }
                }
                if (i == 1) {
                    if (dialogListener!=null) {
                        dialogListener.onPrintok("02");
                    }
                }
                if (i == 2) {
                    if (dialogListener!=null) {
                        dialogListener.onPrintok("03");
                    }
                }
                if (i == 3) {
                    if (dialogListener!=null) {
                        dialogListener.onPrintok("04");
                    }
                }
                if (i == 4) {
                    if (dialogListener!=null) {
                        dialogListener.onPrintok("05");
                    }
                }
                bl[i] = false;
            }
        }
    }

    //重写simpleadapterd的getview方法
    class SetSimpleAdapter extends SimpleAdapter {

        public SetSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
                                int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(context, R.layout.print_dialoglistitem, null);
            }
            CheckBox ckBox = (CheckBox) convertView.findViewById(R.id.X_checkbox);
            //每次都根据 bl[]来更新checkbox
            if (bl[position] == true) {
                ckBox.setChecked(true);
            } else if (bl[position] == false) {
                ckBox.setChecked(false);
            }
            return super.getView(position, convertView, parent);
        }
    }


    class ItemOnClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int p, long id) {
            CheckBox cBox = (CheckBox) view.findViewById(R.id.X_checkbox);
            int position = (p);
            //单选操作
            if (cBox.isChecked()) {
                cBox.setChecked(false);
            } else {
                Log.i("TAG", "取消该选项");
                for (int i = 0; i < bl.length; i++) {
                    bl[i] = false;
                }
                bl[position] = true;
                adapter.notifyDataSetChanged();
            }
        }

    }
}
