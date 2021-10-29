package com.edc.xiaoyaoyouble;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.File;
import java.util.List;

import bean.ExcelUtil;
import bean.Reissue;
import bean.ReissueEventBus;
import bean.ReissueToRom;


import static org.litepal.LitePalApplication.getContext;

public class ReissueActivity extends AppCompatActivity {

    private ListView mListView;
    private List<Reissue> mAll;
    private ReissueAdapter mReissueAdapter;
    static int countX = 0;
    static int countY = 0;
    private TextView tvPathName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reissue);
        initView();
    }


    public void exportFileToReciver(String path, String[] title) {
        //将数据导入乘excel格式
        String filePath = Environment.getExternalStorageDirectory() + path;
        // String filePath = getApplicationContext().getFilesDir().getAbsolutePath()+"/AndroidExcelDemo";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        //我们的测试程序增加一个功能，像商用APP一样的导出文件功能，导出的内容是你存在“所有数据页面”里的数据，导出文件名称就以当前连接的“CGM名称”、“_"和“MAC地址（去中间冒号）”三者组成，

        // title = {"t", "I0t", "I1t", "I2t", "I3t", "I4t", "I5t", "k", "Sg0t", "Sg1t", "Sg2t"};
        String sheetName = "demoSheetName";
        //CaseBean currentCaseBean = new CountDataUtils().getCurrentCaseBean();
        List<Reissue> all1;
        String deviceName = getIntent().getStringExtra("deviceName");
        String deviceAddress = getIntent().getStringExtra("deviceAddress");
        String replace;
        if (deviceAddress==null) {
            replace = "所有设备数据";
        } else {
            replace = deviceAddress.replace(":", "_");
        }

        if (deviceName == null) {
            all1 = LitePal.findAll(Reissue.class);
            deviceName="";
        } else {
            all1 = LitePal.where("Blename = ?", deviceName).find(Reissue.class);
        }

        String excelFileName = "/" + deviceName + "(" + replace +")"+ ".xls";
        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, title);
        ExcelUtil.writeObjListToExcelToReceive(all1, filePath, getContext());//此处需要注意,函数里面会调用对应的Bean类,所以每次重新生成的bean对象,需要重写此函数
        tvPathName.setText(filePath);
        Toast.makeText(this, "数据已导出,路径为" + filePath, Toast.LENGTH_LONG).show();
        // shareFile(getContext(),new File(filePath));
    }

    List<ReissueToRom> mReissueToRoms;

    public void exportFileToReciverToRom(String path, String[] title) {
        //将数据导入乘excel格式
        String filePath = Environment.getExternalStorageDirectory() + path;
        // String filePath = getApplicationContext().getFilesDir().getAbsolutePath()+"/AndroidExcelDemo";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        //我们的测试程序增加一个功能，像商用APP一样的导出文件功能，导出的内容是你存在“所有数据页面”里的数据，导出文件名称就以当前连接的“CGM名称”、“_"和“MAC地址（去中间冒号）”三者组成，

        // title = {"t", "I0t", "I1t", "I2t", "I3t", "I4t", "I5t", "k", "Sg0t", "Sg1t", "Sg2t"};
        String sheetName = "demoSheetName";
        //CaseBean currentCaseBean = new CountDataUtils().getCurrentCaseBean();
        List<ReissueToRom> all1;
        String deviceName = getIntent().getStringExtra("deviceName");
        String deviceAddress = getIntent().getStringExtra("deviceAddress");
        String replace;
        if(deviceAddress==null){
            replace = "所有设备数据";
        }else {
            replace = deviceAddress.replace(":", "_");
        }

        if (deviceName == null) {
            all1 = LitePal.findAll(ReissueToRom.class);
            deviceName="";
        } else {
            all1 = LitePal.where("Blename = ?", deviceName).find(ReissueToRom.class);
        }
        String excelFileName = "/" + deviceName + "(" + replace+")" + ".xls";
        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, title);
        ExcelUtil.writeObjListToExcelToReceiveRom(all1, filePath, getContext());//此处需要注意,函数里面会调用对应的Bean类,所以每次重新生成的bean对象,需要重写此函数
        //Toast.makeText(this, "数据已导出,路径为" + filePath, Toast.LENGTH_LONG).show();
        // shareFile(getContext(),new File(filePath));

    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv_reissue);
        Button deleteAll = (Button) findViewById(R.id.tv_delete_all);
        Button tvExportAll;
        Button tvReadRom;

        tvReadRom = (Button) findViewById(R.id.tv_read_rom);


        tvPathName = (TextView) findViewById(R.id.tv_path_name);

        tvReadRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReissueToRoms = LitePal.findAll(ReissueToRom.class);
                mListView.setAdapter(new ReissueAdapterToRom());
            }
        });

        tvExportAll = (Button) findViewById(R.id.tv_export_all);


        tvExportAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   exportFileToReciver("/AndroidTestUtilsExcelReciver", new String[]{"详细数据", "时间", "编号", "名字"});
                exportFileToReciverToRom("/AndroidTestUtilsExcelReciverRom", new String[]{"详细数据", "时间", "编号", "名字"});*/
                exportFileToReciver("/EDC_Reciver", new String[]{"详细数据", "时间", "编号", "名字"});
                exportFileToReciverToRom("/EDC_ReciverRom", new String[]{"详细数据", "时间", "编号", "名字"});
            }
        });


        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(Reissue.class);
                LitePal.deleteAll(ReissueToRom.class);
                mAll.clear();
                mReissueAdapter.notifyDataSetChanged();
            }
        });
        mReissueAdapter = new ReissueAdapter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAll = LitePal.findAll(Reissue.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(mReissueAdapter);
                    }
                });
            }
        }).start();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] split = mAll.get(position).getData().split(",");
                byte data[] = new byte[split.length];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (int) Integer.parseInt(split[i], 16);
                }
                EventBus.getDefault().post(new ReissueEventBus(data));
                //onBackPressed();
//                Intent intent = new Intent(ReissueActivity.this,MainActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putByteArray("reissue",data);
//                intent.putExtras(bundle);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//设置不要刷新将要跳转的界面
//                startActivity(intent);
                finish();
            }
        });


        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    try {
                        countX = view.getFirstVisiblePosition();
                        countY = view.getChildAt(0).getTop();
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //count=firstVisibleItem;
            }
        });
        //Toast.makeText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.setSelectionFromTop(countX, countY);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class ReissueAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAll.size();
        }

        @Override
        public Object getItem(int position) {
            return mAll.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView name;
            TextView number;
            TextView context;
            TextView detailData;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {  //
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_reissue_detaill, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_data);
                holder.number = (TextView) convertView.findViewById(R.id.tv_number);
                holder.detailData = (TextView) convertView.findViewById(R.id.tv_detail_data);
                holder.context = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //设置holder
            holder.name.setText(mAll.get(position).getBlename());
            holder.number.setText(String.valueOf(mAll.get(position).getNumber()));
            holder.detailData.setText(mAll.get(position).getDetailData());
            holder.context.setText(mAll.get(position).getData());
            return convertView;
        }
    }


    class ReissueAdapterToRom extends BaseAdapter {

        @Override
        public int getCount() {
            return mReissueToRoms.size();
        }

        @Override
        public Object getItem(int position) {
            return mReissueToRoms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView name;
            TextView number;
            TextView context;
            TextView detailData;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {  //
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_reissue_detaill, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_data);
                holder.number = (TextView) convertView.findViewById(R.id.tv_number);
                holder.detailData = (TextView) convertView.findViewById(R.id.tv_detail_data);
                holder.context = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //设置holder
            holder.name.setText(mReissueToRoms.get(position).getBlename());
            holder.number.setText(String.valueOf(mReissueToRoms.get(position).getNumber()));
            holder.detailData.setText(mReissueToRoms.get(position).getDetailData());
            holder.context.setText(mReissueToRoms.get(position).getData());
            return convertView;
        }
    }
}
