package com.kays.handwriting;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root; EditText editor; ImageView sample; TextView preview; Spinner mode; int ink=0xff1a1a1a; boolean lined=false; Uri sampleUri;
  int red=0xffb40019;
  @Override public void onCreate(Bundle b){super.onCreate(b); show();}
  TextView title(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xffffffff);t.setPadding(12,12,12,12);return t;}
  Button button(String label,Runnable r){Button b=new Button(this);b.setText(label);b.setTextColor(0xffffffff);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(red));b.setOnClickListener(v->r.run());root.addView(b);return b;}
  void show(){
    ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff120e10);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,28,24,28);scroll.addView(root);setContentView(scroll);
    root.addView(title("🕷  KAY'S HANDWRITING STUDIO",23));
    root.addView(title("Offline letter composer • Handwriting sample reference",14));
    button("Upload handwriting photo",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,42);});
    sample=new ImageView(this);sample.setAdjustViewBounds(true);sample.setMaxHeight(350);root.addView(sample);
    String saved=getPreferences(0).getString("sample",null);if(saved!=null)try{sampleUri=Uri.parse(saved);sample.setImageURI(sampleUri);}catch(Exception ignored){}
    root.addView(title("Write your letter",17));
    editor=new EditText(this);editor.setTextColor(-1);editor.setHintTextColor(0xffaaaaaa);editor.setHint("Type the letter you want to create");editor.setMinLines(5);editor.setGravity(Gravity.TOP);editor.setText(getPreferences(0).getString("draft",""));root.addView(editor);
    mode=new Spinner(this);String[] options={"Original-style cursive preview","Clean handwriting preview"};ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options);mode.setAdapter(adapter);root.addView(mode);
    button("Ink: black / blue / red",()->{ink=ink==0xff1a1a1a?0xff173e99:ink==0xff173e99?0xffaa1629:0xff1a1a1a;render();});
    button("Toggle lined paper",()->{lined=!lined;render();});
    button("Preview handwritten letter",this::render);
    preview=new TextView(this);preview.setTextSize(25);preview.setTextColor(ink);preview.setPadding(30,35,30,35);preview.setMinHeight(330);preview.setTypeface(android.graphics.Typeface.create("cursive",0));root.addView(preview);
    button("Save draft locally",()->{getPreferences(0).edit().putString("draft",editor.getText().toString()).apply();Toast.makeText(this,"Draft saved",0).show();});
    button("Share letter as PNG",this::share);
    root.addView(title("Note: This version uses a cursive font, not AI reconstruction of the uploaded handwriting. Generated writing is synthetic.",13));
    render();
  }
  void render(){if(preview==null)return;preview.setText(editor.getText().toString());preview.setTextColor(ink);preview.setTypeface(Typeface.create(mode.getSelectedItemPosition()==0?"cursive":"serif",mode.getSelectedItemPosition()==0?0:Typeface.ITALIC));preview.setBackgroundColor(lined?0xffeaf2ff:0xfffff9ed);}
  void share(){render();try{
    int w=1080,h=Math.max(1400,preview.getHeight()*3);Bitmap bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bmp);c.drawColor(lined?0xffeaf2ff:0xfffff9ed);
    Paint p=new Paint(3);if(lined){p.setColor(0xffb8d1e8);p.setStrokeWidth(2);for(int y=110;y<h;y+=85)c.drawLine(40,y,w-40,y,p);}
    p.setColor(ink);p.setTextSize(43);p.setTypeface(Typeface.create(mode.getSelectedItemPosition()==0?"cursive":"serif",Typeface.ITALIC));
    String text=editor.getText().toString();int y=95;for(String paragraph:text.split("\\n",-1)){String line="";for(String word:paragraph.split(" ")){String test=line.isEmpty()?word:line+" "+word;if(p.measureText(test)>w-100){c.drawText(line,50,y,p);y+=85;line=word;}else line=test;}c.drawText(line,50,y,p);y+=85;if(y>h-80)break;}
    android.content.ContentValues values=new android.content.ContentValues();
    values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,"Kays-Letter-"+System.currentTimeMillis()+".png");
    values.put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png");
    values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,"Pictures/KaysHandwritingStudio");
    Uri uri=getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    if(uri==null)throw new IOException("Cannot create image");
    try(java.io.OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException("Cannot open image");bmp.compress(Bitmap.CompressFormat.PNG,100,out);}
    Intent i=new Intent(Intent.ACTION_SEND);i.setType("image/png");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share generated letter"));
  }catch(Exception e){Toast.makeText(this,"Export failed: "+e.getMessage(),1).show();}}
  @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==42&&res==RESULT_OK&&data!=null){sampleUri=data.getData();try{getContentResolver().takePersistableUriPermission(sampleUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}sample.setImageURI(sampleUri);getPreferences(0).edit().putString("sample",sampleUri.toString()).apply();}}
}