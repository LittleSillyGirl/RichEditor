package com.yuruiyin.richeditor.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuruiyin.richeditor.RichTextView;
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;
import com.yuruiyin.richeditor.model.BlockSpanBean;
import com.yuruiyin.richeditor.model.RichBlockBean;
import com.yuruiyin.richeditor.sample.model.GameVm;
import com.yuruiyin.richeditor.span.AtSpan;
import com.yuruiyin.richeditor.span.BlockImageSpan;

import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private final String TAG = "PreviewActivity";

    private String content;
    private RichTextView richTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        richTextView = findViewById(R.id.richTextView);
        Intent intent = getIntent();
        if (intent != null) {
            content = intent.getStringExtra("content");
        }
        Log.i(TAG, "onCreate: content==" + content);
        if (TextUtils.isEmpty(content) || TextUtils.equals("[]", content)) {
            content = "[\n" +
                    "    {\n" +
                    "        \"blockType\": \"normal_text\",\n" +
                    "        \"inlineStyleEntityList\": [\n" +
                    "            {\n" +
                    "                \"inlineType\": \"underline\",\n" +
                    "                \"length\": 10,\n" +
                    "                \"offset\": 0\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"strike_through\",\n" +
                    "                \"length\": 10,\n" +
                    "                \"offset\": 0\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"bold\",\n" +
                    "                \"length\": 10,\n" +
                    "                \"offset\": 0\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"italic\",\n" +
                    "                \"length\": 10,\n" +
                    "                \"offset\": 0\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineSpanObtainObject\": {\n" +
                    "                    \"inlineType\": \"at\",\n" +
                    "                    \"key\": \"3\",\n" +
                    "                    \"textColor\": \"#31BC63\",\n" +
                    "                    \"textSize\": -1.0,\n" +
                    "                    \"value\": \"@昵称3\"\n" +
                    "                },\n" +
                    "                \"inlineType\": \"at\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 10\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineSpanObtainObject\": {\n" +
                    "                    \"inlineType\": \"topic\",\n" +
                    "                    \"key\": \"6\",\n" +
                    "                    \"textColor\": \"#31BC63\",\n" +
                    "                    \"textSize\": -1.0,\n" +
                    "                    \"value\": \"#昵称6\"\n" +
                    "                },\n" +
                    "                \"inlineType\": \"topic\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 15\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"underline\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 20\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"strike_through\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 20\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"bold\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 20\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineType\": \"italic\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 20\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"inlineSpanObtainObject\": {\n" +
                    "                    \"inlineType\": \"topic\",\n" +
                    "                    \"key\": \"2\",\n" +
                    "                    \"textColor\": \"#31BC63\",\n" +
                    "                    \"textSize\": -1.0,\n" +
                    "                    \"value\": \"#昵称2\"\n" +
                    "                },\n" +
                    "                \"inlineType\": \"topic\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 24\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"text\": \"来啦来啦爸教师节快乐@昵称3 #昵称6 来啦啦吧#昵称2 \"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"blockSpanObtainObject\": {\n" +
                    "            \"height\": 0,\n" +
                    "            \"id\": \"2\",\n" +
                    "            \"isFromDraft\": false,\n" +
                    "            \"isGif\": false,\n" +
                    "            \"isLong\": true,\n" +
                    "            \"isPhoto\": true,\n" +
                    "            \"maxHeight\": 0,\n" +
                    "            \"maxWidth\": 990,\n" +
                    "            \"path\": \"/storage/emulated/0/PictureTest/saveTemp.jpg\",\n" +
                    "            \"width\": 0\n" +
                    "        },\n" +
                    "        \"blockType\": \"image\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"blockType\": \"normal_text\",\n" +
                    "        \"inlineStyleEntityList\": [\n" +
                    "            {\n" +
                    "                \"inlineSpanObtainObject\": {\n" +
                    "                    \"inlineType\": \"topic\",\n" +
                    "                    \"key\": \"4\",\n" +
                    "                    \"textColor\": \"#31BC63\",\n" +
                    "                    \"textSize\": -1.0,\n" +
                    "                    \"value\": \"#昵称4\"\n" +
                    "                },\n" +
                    "                \"inlineType\": \"topic\",\n" +
                    "                \"length\": 4,\n" +
                    "                \"offset\": 7\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"text\": \"就理两就理两句#昵称4 \"\n" +
                    "    }\n" +
                    "]";
        }
        List<RichBlockBean> richBlockBeanList = new Gson().fromJson(content, new TypeToken<List<RichBlockBean>>() {
        }.getType());
        richTextView.setOnRichClickListener(o -> {
            Log.i(TAG, "onCreate: 点击");
            if (o instanceof AtSpan) {
                Log.i(TAG, "onClick: 点击了@用户或者话题");
                AtSpan atSpan = (AtSpan) o;
                Toast.makeText(PreviewActivity.this, "点击了类型为" + atSpan.getType() + "的" + atSpan.getInlineSpanBean().getValue(), Toast.LENGTH_SHORT).show();
            }else if (o instanceof BlockImageSpan) {
                Log.i(TAG, "onClick: 点击了图片");
                BlockImageSpan blockImageSpan = (BlockImageSpan) o;
                BlockSpanBean blockSpanObtainObject = blockImageSpan.getBlockSpanObtainObject();
                Toast.makeText(PreviewActivity.this, "点击了类型为" + blockSpanObtainObject.getType() + "的" + blockSpanObtainObject.getPath(), Toast.LENGTH_SHORT).show();
            }
        });
        richTextView.showContent(richBlockBeanList, richBlockBean -> {
            switch (richBlockBean.getBlockType()){
                case BlockSpanEnum.GAME:
                    View gameItemView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.editor_game_item, null);
                    ImageView ivGameIcon = gameItemView.findViewById(R.id.ivGameIcon);
                    TextView tvGameName = gameItemView.findViewById(R.id.tvGameName);
                    ivGameIcon.setImageResource(R.mipmap.icon_game_zhuoyao);
                    BlockSpanBean blockSpanBean = richBlockBean.getBlockSpan();
                    Gson gson = new Gson();
                    String gameVmStr = gson.toJson(blockSpanBean.getSpanObtainObject());
                    GameVm gameVm = gson.fromJson(gameVmStr, GameVm.class);
                    tvGameName.setText(gameVm.getName());
                    return gameItemView;
                default:
            }
            return null;
        });
    }
}