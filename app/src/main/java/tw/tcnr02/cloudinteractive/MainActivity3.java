package tw.tcnr02.cloudinteractive;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class MainActivity3 extends AppCompatActivity implements View.OnClickListener {

    private ImageView showimageview;
    private TextView showid;
    private TextView showtitle;
    private String ID, Title, ThumbnailUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apishow);
        Bundle bundle = this.getIntent().getExtras();
        ID = bundle.getString("id");
        Title = bundle.getString("title");
        ThumbnailUrl = bundle.getString("thumbnailUrl");
        setupViewComponent();
    }

    private void setupViewComponent() {
       showimageview = (ImageView)findViewById(R.id.apitext_imageview);
       showid = (TextView)findViewById(R.id.apitext_textid);
       showtitle = (TextView)findViewById(R.id.apitext_texttitle);


       Picasso.get().load(ThumbnailUrl).into(showimageview);
       showimageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
       showid.setText(ID);
       showtitle.setText(Title);
        showimageview.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        this.finish();
    }
}
