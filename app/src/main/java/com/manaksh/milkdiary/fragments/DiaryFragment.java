package com.manaksh.milkdiary.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.manaksh.milkdiary.adapter.ImageAdapter;
import com.manaksh.milkdiary.model.DailyData;
import com.manaksh.milkdiary.model.ItemType;
import com.manaksh.milkdiary.model.TransactionType;
import com.manaksh.milkdiary.utils.Constants;
import com.manaksh.milkdiary.utils.FileOperationsImpl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import manaksh.com.milkdiary.R;

public class DiaryFragment extends Fragment implements FragmentLifecycle {

    static String[] tags = new String[]{"#tag1", "#tag2", "#tag3", "#tag4"};
    Context context = null;
    GridView grid, gridTag = null;
    HashMap<Integer, DailyData> ls_databean = new HashMap<Integer, DailyData>();
    //List<DailyData> ls_databean = new ArrayList<DailyData>();
    ImageButton btn_Save = null;
    ArrayList<DailyData> reports = new ArrayList<DailyData>();
    StringBuilder _thisDay = null;
    /*
    Initializes the DataPicker
     */
    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            ls_databean.clear();
            ImageAdapter adapterObj = new ImageAdapter(context);
            grid = (GridView) rootView.findViewById(R.id.valueGrid);
            grid.setAdapter(adapterObj);
            // arg1 = year
            // arg2 = month
            // arg3 = day
            StringBuilder date = showDate(arg1, arg2 + 1, arg3);
            if (!(date.equals(_thisDay))) {
                setView();
            }
        }
    };
    ImageButton arrowLeft, arrowRight = null;
    TextView calenderBtn = null;
    View rootView = null;
    private DatePicker datePicker = null;
    private Calendar calendar = null;
    private TextView dateView = null;
    private int year, month, day = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_diary, container, false);
        this.context = getActivity().getBaseContext();
        //this.context = getActivity().getApplicationContext();
        this.rootView = rootView;
        //READ TAGS FILE and load the #tags
        ArrayList<String> tagList = FileOperationsImpl.readFromFile(getActivity().getBaseContext(), Constants.TAGS_FILE);

        if (tagList == null) {
            tags = new String[]{"#tag1", "#tag2", "#tag3", "#tag4"};
        } else {
            int i = 0;
            for (String str : tagList) {
                tags[i] = str;
                i++;
            }
        }
        //Initializes the calendarview
        dateView = (TextView) rootView.findViewById(R.id.dateView);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        _thisDay = showDate(year, month + 1, day);

        //reports = FileOperationsImpl.readFromFile(getActivity().getBaseContext(), Constants.REPORTS_FILE);
        reports = null;

        final ImageAdapter adapterObj = new ImageAdapter(context);
        //adapterObj = loadAdapter(reports, dateView, adapterObj);

       // Date,ORANGE,2.5,HIT
        if (reports != null) {

            for (DailyData data : reports) {

                //String[] data = str.split(",");

                //split with . & form the image name
                //String[] image_name = data[2].split("\\.");

                String[] image_name = new Double(data.getQuantity()).toString().split("\\.");

                if (data.getDate().equals(dateView.getText().toString())) {

                    int idNo = getResources().getIdentifier("_" + image_name[0] + "_" + image_name[1] + "_" + data.getTransactionType(), "mipmap", context.getPackageName());
                    int position = getPosition(new Double(data.getQuantity()).toString(), data.getTransactionType().toString());
                    adapterObj.mThumbIds[position] = idNo;
                }
            }
        }

        final Integer[] mThumbs = adapterObj.mThumbIds;
        grid = (GridView) rootView.findViewById(R.id.valueGrid);
        grid.setAdapter(adapterObj);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View imgView, int position, long id) {
                ImageView imageView = (ImageView) imgView;
                String name = getResources().getResourceEntryName(mThumbs[position]);
                //_1_0_steady
                String[] splitName = name.split("_");
                int idNo = 0;
                ItemType _type = Constants.PositionTypeMap.get(position);

                if (position == 0 || position == 1 || position == 2 || position == 3) {
                } else {
                    //Create a new object and set everything
                    DailyData dailyData = new DailyData();

                    if (dateView != null) {
                        dailyData.setDate(dateView.getText().toString());

                    } else {
                        dailyData.setDate("01/01/2000");
                    }

                    dailyData.setType(_type);
                    dailyData.setQuantity(Double.parseDouble(splitName[1] + "." + splitName[2]));

                    switch (splitName[3]) {
                        //DEFAULT->HIT
                        case Constants.STEADY:
                            idNo = getResources().getIdentifier("_" + splitName[1] + "_" + splitName[2] + "_hit", "mipmap", context.getPackageName());
                            imageView.setImageResource(idNo);
                            adapterObj.mThumbIds[position] = idNo;
                            dailyData.setTransactionType(TransactionType.hit);
                            break;
                        //HIT->MISS
                        case Constants.HIT:
                            idNo = getResources().getIdentifier("_" + splitName[1] + "_" + splitName[2] + "_miss", "mipmap", context.getPackageName());
                            imageView.setImageResource(idNo);
                            adapterObj.mThumbIds[position] = idNo;
                            dailyData.setTransactionType(TransactionType.miss);
                            break;
                        //MISS->DEFAULT
                        case Constants.MISS:
                            idNo = context.getResources().getIdentifier("_" + splitName[1] + "_" + splitName[2] + "_steady", "mipmap", context.getPackageName());
                            imageView.setImageResource(idNo);
                            adapterObj.mThumbIds[position] = idNo;
                            dailyData.setTransactionType(TransactionType.steady);
                            break;
                    }
                    //ls_databean.add(dailyData);
                    if (ls_databean.containsKey(position)) {
                        //update
                        //DailyData d = ls_databean.get(position);

                        if (ls_databean.get(position).getTransactionType().equals(dailyData.getTransactionType())) {
                        } else {
                            ls_databean.remove(position);
                            ls_databean.put(position, dailyData);
                        }
                        //d=dailyData;
                    } else {
                        ls_databean.put(position, dailyData);
                    }
                }
            }
        });

        //#Tags Grid
        gridTag = (GridView) rootView.findViewById(R.id.labelGrid);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, tags);
        gridTag.setAdapter(adapter);
        gridTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    final int position, long id) {

                ls_databean.clear();
                final View v1 = v;

                //get prompt.xml view
                LayoutInflater layoutInflater = LayoutInflater.from(context);

                View promptView = layoutInflater.inflate(R.layout.prompts, null);
                //View promptView = layoutInflater.inflate(R.layout.prompts, parent,false);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set prompts.xml to be the layout file of the alertdialog builder
                alertDialogBuilder.setView(promptView);

                //Get new tag from the user
                final EditText input = (EditText) promptView.findViewById(R.id.userInput);

                // setup a dialog window
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                ((TextView) v1).setText("#" + input.getText());

                                //update tags array and update the file
                                String newTag = "#" + input.getText().toString();
                                tags[position] = newTag;
                                String allTags = "";

                                for (int i = 0; i < tags.length; i++) {
                                    allTags = allTags + tags[i] + ",";
                                }
                                FileOperationsImpl.writeToFile(context, allTags);
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alertD = alertDialogBuilder.create();

                alertD.show();
            }
        });

        //Save Button
        btn_Save = (ImageButton) rootView.findViewById(R.id.saveBtn);
        btn_Save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ls_databean.size() == 0) {
                    Toast.makeText(context, Constants.NO_CHANGE, Toast.LENGTH_SHORT).show();
                } else {
                    boolean _result = FileOperationsImpl.writeToFile(context, ls_databean);

                    if (_result) {
                        Toast.makeText(context, Constants.FILE_SAVE_SUCCESS, Toast.LENGTH_SHORT).show();
                        ls_databean.clear();
                    } else {
                        Toast.makeText(context, Constants.FILE_SAVE_FAILURE, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpdFromDate = new DatePickerDialog(getActivity(), myDateListener, year, month, day);
                dpdFromDate.show();
            }
        });


        arrowLeft = (ImageButton) rootView.findViewById(R.id.arrowLeft);
        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {

                    Date date = formatter.parse(dateView.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.DATE, -1);
                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                    _thisDay = showDate(year, month + 1, day);
                    setView();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        arrowRight = (ImageButton) rootView.findViewById(R.id.arrowRight);
        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date = formatter.parse(dateView.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.DATE, 1);
                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                    _thisDay = showDate(year, month + 1, day);
                    setView();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
    }

    /*
    Sets the default Diary View after initializing all the default/steady images
     */
    public void setView() {
        ImageAdapter adapterObj = new ImageAdapter(context);
        //reports = FileOperationsImpl.readFromFile(context, Constants.REPORTS_FILE);

        reports = null;

        if ((reports != null) && (reports.size() != 0)) {
            for (DailyData data : reports) {

                //String[] data = str.split(",");
                //split with . & form the image name
                //String[] image_name = data[2].split("\\.");
                String[] image_name = new Double(data.getQuantity()).toString().split("\\.");

                if (data.getDate().equals(dateView.getText().toString())) {
                    String txt = "_" + image_name[0] + "_" + image_name[1] + "_" + data.getTransactionType();
                    int idNo = getResources().getIdentifier("_" + image_name[0] + "_" + image_name[1] + "_" + data.getTransactionType(), "mipmap", context.getPackageName());
                    int position = getPosition(new Double(data.getQuantity()).toString(), data.getTransactionType().toString());
                    adapterObj.mThumbIds[position] = idNo;
                }
            }
        } else {
        }
        grid = (GridView) getActivity().findViewById(R.id.valueGrid);
        grid.setAdapter(adapterObj);
    }

    /*
    Sets & Shows the selected date on the dateView
     */
    private StringBuilder showDate(int year, int month, int day) {
        StringBuilder date = new StringBuilder().append(day).append("/").
                append(month).append("/").append(year);
        dateView.setText(date);
        return date;
    }

    /*
    Returns the Grid position of selected value
     */
    public int getPosition(String input, String type) {
        //compute position
        int position = 0;
        if (type.equals(Constants.TYPE1)) {
            position = Constants.Type1Map.get(input);
        } else if (type.equals(Constants.TYPE2)) {
            position = Constants.Type2Map.get(input);
        } else if (type.equals(Constants.TYPE3)) {
            position = Constants.Type3Map.get(input);
        } else if (type.equals(Constants.TYPE4)) {
            position = Constants.Type4Map.get(input);
        }
        return position;
    }
}
