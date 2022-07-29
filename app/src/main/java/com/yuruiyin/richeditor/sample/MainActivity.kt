package com.yuruiyin.richeditor.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yuruiyin.richeditor.enumtype.BlockSpanEnum
import com.yuruiyin.richeditor.enumtype.FileTypeEnum
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum
import com.yuruiyin.richeditor.enumtype.InlineSpanEnum.TOPIC
import com.yuruiyin.richeditor.model.*
import com.yuruiyin.richeditor.sample.model.*
import com.yuruiyin.richeditor.sample.utils.DeviceUtil
import com.yuruiyin.richeditor.sample.utils.JsonUtil
import com.yuruiyin.richeditor.sample.utils.WindowUtil
import com.yuruiyin.richeditor.utils.BitmapUtil
import com.yuruiyin.richeditor.utils.FileUtil
import com.yuruiyin.richeditor.utils.LogUtil
import com.yuruiyin.richeditor.utils.ViewUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val GET_PHOTO_REQUEST_CODE = 1
        const val GET_VIDEO_REQUEST_CODE = 2
        const val AT_USER = 3
        const val TOPIC_CONTENT = 4
        const val TAG = "MainActivity"

        // 测试用
        const val HUAWEI_IMAGE_PATH =
            "/storage/emulated/0/Huawei/MagazineUnlock/magazine-unlock-04-2.3.4312-_6DE13B88E5CE3D69DE5469945117A2A6.jpg"
        const val MUMU_IMAGE_PATH = "/storage/emulated/0/DCIM/challenge/122489.jpg"
        const val OPPO_IMAGE_PATH =
            "/storage/emulated/0/Pictures/magazine-unlock-04-2.3.4312-_6DE13B88E5CE3D69DE5469945117A2A6.jpg";

        /**
         * 草稿SharePreferences的名字
         */
        const val SP_DRAFT_NAME = "rich_editor"

        /**
         * 保存在SharePreferences中的草稿json数据key
         */
        const val KEY_DRAFT_JSON = "key_draft_json"
    }

    private val editorPaddingLeft by lazy {
        resources.getDimension(R.dimen.editor_padding_left)
    }

    private val editorPaddingRight by lazy {
        resources.getDimension(R.dimen.editor_padding_right)
    }

    public val imageWidth by lazy {
        resources.getDimension(R.dimen.editor_image_width).toInt()
    }

    private val imageMaxHeight by lazy {
        resources.getDimension(R.dimen.editor_image_max_height).toInt()
    }

    private val screenWidth by lazy {
        WindowUtil.getScreenSize(this)[0]
    }

    private val gameItemHeight by lazy {
        resources.getDimension(R.dimen.editor_game_height).toInt()
    }

    private val gameIconSize by lazy {
        resources.getDimension(R.dimen.editor_game_icon_size).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerEvents()
    }


    /**
     * 粗体
     */
    private fun initBold() {
        val styleBtnVm = StyleBtnBean.Builder()
            .setType(InlineSpanEnum.BOLD)
            .setIvIcon(ivBold)
            .setIconNormalResId(R.mipmap.icon_bold_normal)
            .setIconLightResId(R.mipmap.icon_bold_light)
            .setClickedView(ivBold)
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    /**
     * 斜体
     */
    private fun initItalic() {
        val styleBtnVm = StyleBtnBean.Builder()
            .setType(InlineSpanEnum.ITALIC)
            .setIvIcon(ivItalic)
            .setIconNormalResId(R.mipmap.icon_italic_normal)
            .setIconLightResId(R.mipmap.icon_italic_light)
            .setClickedView(ivItalic)
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    /**
     * 删除线
     */
    private fun initStrikeThrough() {
        val styleBtnVm = StyleBtnBean.Builder()
            .setType(InlineSpanEnum.STRIKE_THROUGH)
            .setIvIcon(ivStrikeThrough)
            .setIconNormalResId(R.mipmap.icon_strikethrough_normal)
            .setIconLightResId(R.mipmap.icon_strikethrough_light)
            .setClickedView(ivStrikeThrough)
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    /**
     * 下划线
     */
    private fun initUnderline() {
        val styleBtnVm = StyleBtnBean.Builder()
            .setType(InlineSpanEnum.UNDERLINE)
            .setIvIcon(ivUnderline)
            .setIconNormalResId(R.mipmap.icon_underline_normal)
            .setIconLightResId(R.mipmap.icon_underline_light)
            .setClickedView(ivUnderline)
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    /**
     * 标题
     */
    private fun initHeadline() {
//        val styleBtnVm = StyleBtnVm.Builder()
//            .setType(RichTypeEnum.BLOCK_HEADLINE)  // 指定为段落标题类型
//            .setIvIcon(ivHeadline)       // 图标ImageView，用于修改高亮状态
//            .setIconNormalResId(R.mipmap.icon_headline_normal)  // 正常图标
//            .setIconLightResId(R.mipmap.icon_headline_light)    // 高亮图标
//            .setClickedView(vgHeadline)  // 指定被点击的view
//            .setTvTitle(tvHeadline)      // 按钮标题文字
//            .setTitleNormalColor(
//                ContextCompat.getColor(
//                    this@MainActivity,
//                    R.color.headline_normal_text_color
//                )
//            ) // 正常标题文字颜色
//            .setTitleLightColor(
//                ContextCompat.getColor(
//                    this@MainActivity,
//                    R.color.headline_light_text_color
//                )
//            )   // 高亮标题文字颜色
//            .build()
//
//        richEditText.initStyleButton(styleBtnVm)
    }

    /**
     * 下划线
     */
    private fun initBlockQuote() {
//        val styleBtnVm = StyleBtnVm.Builder()
//            .setType(RichTypeEnum.BLOCK_QUOTE)
//            .setIvIcon(ivBlockquote)
//            .setIconNormalResId(R.mipmap.icon_blockquote_normal)
//            .setIconLightResId(R.mipmap.icon_blockquote_light)
//            .setClickedView(ivBlockquote)
//            .build()
//
//        richEditText.initStyleButton(styleBtnVm)
    }

    public fun registerEvents() {
        // 生成json数据，显示到TextView上
        btnCreateJson.setOnClickListener {
            showJson(richEditText.richBlockBeanList)
        }

        //预览
        btnPreview.setOnClickListener {
            startActivity(Intent(this@MainActivity, PreviewActivity::class.java).apply {
                putExtra("content", Gson().toJson(richEditText.richBlockBeanList))
            })
        }

        // 清空内容
        btnClearContent.setOnClickListener {
            richEditText.clearContent()
        }

        // 保存草稿
        btnSaveDraft.setOnClickListener {
            handleSaveDraft()
        }

        // 恢复草稿
        btnRestoreDraft.setOnClickListener {
            handleRestoreDraft()
        }

        // 清空草稿
        btnClearDraft.setOnClickListener {
            handleClearDraft()
        }

        // 粗体
        initBold()

        // 斜体
        initItalic()

        // 删除线
        initStrikeThrough()

        // 下划线
        initUnderline()

        // 标题
//        initHeadline()

        // 引用
//        initBlockQuote()

        // 标题
//        initAt()

        // 标题
//        initTopic()

        // @他人
        ivAt.setOnClickListener {
            startActivityForResult(Intent(this, AtListActivity::class.java), AT_USER)
        }

        // #话题
        ivTopic.setOnClickListener {
            startActivityForResult(Intent(this, AtListActivity::class.java), TOPIC_CONTENT)
        }

        // 添加链接
        ivAddLink.setOnClickListener {
// 1. Instantiate an AlertDialog.Builder with its constructor
            // 1. Instantiate an AlertDialog.Builder with its constructor
            val builder = AlertDialog.Builder(this)

            // 2. Chain together various setter methods to set the dialog characteristics

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle("添加链接")

            @SuppressLint("InflateParams")
            val areInsertLinkView: View =
                layoutInflater.inflate(R.layout.are_link_insert, null)

            builder.setView(areInsertLinkView) // Add the buttons
                .setPositiveButton("确定", DialogInterface.OnClickListener { dialog, id ->
                    val etLinkUrl =
                        areInsertLinkView.findViewById<EditText>(R.id.etLinkUrl)
                    val etLinkName =
                        areInsertLinkView.findViewById<EditText>(R.id.etLinkName)
                    val url = etLinkUrl.text.toString()
                    if (TextUtils.isEmpty(url)) {
                        dialog.dismiss()
                        return@OnClickListener
                    }
                    //感觉有需要的胡，可以加上链接的图标
                    richEditText.insertAt(
                        InlineSpanBean<Any>(
                            InlineSpanEnum.LINK,
                            url,
                            if (TextUtils.isEmpty(etLinkName.text.toString())) url else etLinkName.text.toString(),
                        )
                    ) {
                        Log.i(TAG, "registerEvents: 点击了链接")
                    }
                })
            builder.setNegativeButton(
                "取消"
            ) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }

        // 添加图片
        ivAddImage.setOnClickListener {
            handleAddImage()
        }

        // 添加分割线
        ivAddDivider.setOnClickListener {
            handleAddDivider()
        }

        // 添加游戏（自定义布局的一种）
        ivAddGame.setOnClickListener {
            handleAddGame()
        }

        // 添加同一张图片，用于测试插入多张图片导致的卡顿和OOM问题
        ivAddSameImageForTest.setOnClickListener {
            handleAddSameImageForTest()
        }

        ivUndo.setOnClickListener {
            if (!richEditText.isUndoRedoEnable) {
                Toast.makeText(this, "未开启undo redo功能", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            richEditText.undo()
        }

        ivRedo.setOnClickListener {
            if (!richEditText.isUndoRedoEnable) {
                Toast.makeText(this, "未开启undo redo功能", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            richEditText.redo()
        }

        // 组件内部默认不开启undo redo功能（由于undo redo功能会占用更大的内存）
//        richEditText.isUndoRedoEnable = true
    }

    private fun initAt() {
        val styleBtnVm = StyleBtnBean.Builder()
            //行内ImageSpan
            .setType(InlineSpanEnum.AT)  // 指定为段落标题类型
            .setIvIcon(ivAt)       // 图标ImageView，用于修改高亮状态
            .setIconNormalResId(R.mipmap.ic_at)  // 正常图标
            .setIconLightResId(R.mipmap.ic_at)    // 高亮图标
            .setClickedView(ivAt)  // 指定被点击的view
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    private fun initTopic() {
        val styleBtnVm = StyleBtnBean.Builder()
            //行内ImageSpan
            .setType(InlineSpanEnum.TOPIC)  // 指定为段落标题类型
            .setIvIcon(ivTopic)       // 图标ImageView，用于修改高亮状态
            .setIconNormalResId(R.mipmap.ic_topic)  // 正常图标
            .setIconLightResId(R.mipmap.ic_topic)    // 高亮图标
            .setClickedView(ivTopic)  // 指定被点击的view
            .build()

        richEditText.initStyleButton(styleBtnVm)
    }

    private fun showJson(richBlockBeanList: List<RichBlockBean>) {
        val content = Gson().toJson(richBlockBeanList)
        val formatJsonContent = JsonUtil.getFormatJson(content)
        tvContentJson.text = formatJsonContent
        Log.d(TAG, "\n $formatJsonContent")
    }

    private fun handleClearDraft() {
        val sp = getSharedPreferences(SP_DRAFT_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        editor.apply()

        Toast.makeText(this, "清空草稿成功", Toast.LENGTH_SHORT).show()
    }

    /**
     * 遍历段落恢复草稿，即一段一段的插入到编辑器中
     */
    private fun restoreDraft(richBlockBeanList: List<RichBlockBean>) {
        richEditText.clearContent()
        richBlockBeanList.forEach {
            when (it.blockType) {
                BlockSpanEnum.NORMAL_TEXT, BlockSpanEnum.HEADLINE, BlockSpanEnum.QUOTE -> {
                    richEditText.insertBlockText(it)
                }
                // 以下就是用户自定义的blockType，可能是图片、视频、自定义类型等
                BlockSpanEnum.IMAGE, BlockSpanEnum.VIDEO -> {
                    val iBlockSpanObtainObject = it.blockSpan ?: return@forEach
                    doAddBlockImageSpan(iBlockSpanObtainObject.path, iBlockSpanObtainObject, true)
                }
                BlockSpanEnum.DIVIDER -> {
                    handleAddDivider(true)
                }
                BlockSpanEnum.GAME -> {
//                    val gameVm = it.game ?: return@forEach
//                    doAddGame(gameVm, true)
                }
            }
        }
    }

    /**
     * 恢复草稿
     */
    private fun handleRestoreDraft() {
        val sp = getSharedPreferences(SP_DRAFT_NAME, Context.MODE_PRIVATE)
        val jsonContent = sp.getString(KEY_DRAFT_JSON, "")
        if (TextUtils.isEmpty(jsonContent)) {
            Toast.makeText(this, "没有草稿内容", Toast.LENGTH_SHORT).show()
            return
        }
        Log.i(TAG, "handleRestoreDraft: jsonContent==$jsonContent")
        val richBlockBeanList = Gson().fromJson<List<RichBlockBean>>(
            jsonContent,
            object : TypeToken<List<RichBlockBean>>() {}.type
        )

        showJson(richBlockBeanList)
        restoreDraft(richBlockBeanList)
    }

    /**
     * 保存草稿
     */
    private fun handleSaveDraft() {
        val richEditorBlockList = richEditText.richBlockBeanList
        // 先将对象进行转换，让里头blockImageSpanObtainObject具体到各自类型的实体上（如ImageVm）
        val jsonContent = Gson().toJson(richEditorBlockList)
        val sp = getSharedPreferences(SP_DRAFT_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(KEY_DRAFT_JSON, jsonContent)
//        editor.commit() // commit是同步写，可能会阻塞主线程，因此不建议
        editor.apply()

        Toast.makeText(this, "保存草稿成功", Toast.LENGTH_SHORT).show()
    }

    private fun getEditTextWidthWithoutPadding(): Int {
        // 富文本编辑器编辑区域的宽度, 这个宽度一定要小于编辑器的宽度，否则会出现ImageSpan被绘制两边的情况
        return (screenWidth - editorPaddingLeft - editorPaddingRight - 6).toInt()
    }

    @SuppressLint("InflateParams")
    private fun doAddGame(blockSpanBean: BlockSpanBean<GameVm>, isFromDraft: Boolean = false) {
        val gameItemView = layoutInflater.inflate(R.layout.editor_game_item, null)
        val ivGameIcon = gameItemView.findViewById<ImageView>(R.id.ivGameIcon)
        val tvGameName = gameItemView.findViewById<TextView>(R.id.tvGameName)
        ivGameIcon.setImageResource(R.mipmap.icon_game_zhuoyao)
        tvGameName.text = blockSpanBean.spanObtainObject.name

        ivGameIcon.layoutParams.width = gameIconSize
        ivGameIcon.layoutParams.height = gameIconSize

        val gameItemWidth = getEditTextWidthWithoutPadding()
        ViewUtil.layoutView(gameItemView, gameItemWidth, gameItemHeight)
        blockSpanBean.height = gameItemHeight
        blockSpanBean.maxHeight = imageMaxHeight
        blockSpanBean.width = gameItemWidth
        blockSpanBean.maxWidth = gameItemWidth
        blockSpanBean.isFromDraft = isFromDraft
        Log.i(TAG, "doAddGame: $blockSpanBean")
        richEditText.insertBlockImage(
            BitmapUtil.getBitmap(gameItemView),
            blockSpanBean
        ) { blockImageSpan ->
            val retGameVm = blockImageSpan.blockSpanObtainObject as GameVm
            // 点击游戏item
            Toast.makeText(this, "短按了游戏：${retGameVm.name}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 插入游戏
     */
    private fun handleAddGame() {
        doAddGame(BlockSpanBean<GameVm>(BlockSpanEnum.GAME).apply {
            id = "1"
            spanObtainObject = GameVm("一起来捉妖")
        })
        Log.d(TAG, "EditText的高度： " + richEditText.height)
    }

    /**
     * 添加同一张图片，用于测试插入多张图片导致的卡顿和OOM问题
     */
    fun handleAddSameImageForTest() {
        val realImagePath = if (DeviceUtil.isEmulator(this)) {
            MUMU_IMAGE_PATH
        } else {
            if (android.os.Build.MANUFACTURER == "HUAWEI") {
                HUAWEI_IMAGE_PATH
            } else {
                OPPO_IMAGE_PATH
            }
        }
        doAddBlockImageSpan(
            realImagePath,
            BlockSpanBean<Any>()
        )
        Log.d(TAG, "EditText的高度： " + richEditText.height)
    }

    /**
     * 处理添加分割线，其实插入的也是BlockImageSpan
     */
    private fun handleAddDivider(isFromDraft: Boolean = false) {
        val blockSpanObtainObject = BlockSpanBean<Any>(
            BlockSpanEnum.DIVIDER
        ).apply {
            width = getEditTextWidthWithoutPadding()
            maxWidth = getEditTextWidthWithoutPadding()
            maxHeight = imageMaxHeight
            this.isFromDraft = isFromDraft
        }
        richEditText.insertDivider(R.mipmap.image_divider_line, blockSpanObtainObject)
    }

    /**
     * 处理插入图片
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun handleAddImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        /* 开启Pictures画面Type设定为image */
//        intent.setType("image/*");
        //intent.setType("audio/*"); //选择音频
        //intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
        //intent.setType("video/*;image/*");//同时选择视频和图片


        /* 使用Intent.ACTION_GET_CONTENT这个Action */
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GET_PHOTO_REQUEST_CODE)
    }

    private fun doAddBlockImageSpan(
        realImagePath: String,
        blockSpanBean: BlockSpanBean<Any>, // 不指定宽高，使用图片原始大小（但组件内对最大宽和最大高还是有约束的）
        isFromDraft: Boolean = false
    ) {
//        val blockImageSpanVm =
//            BlockImageSpanBean(
//                blockImageSpanObtainObject,
//                imageWidth,
//                imageMaxHeight
//            ) // 指定宽高
//        blockImageSpanBean.maxWidth = getEditTextWidthWithoutPadding()
//        blockImageSpanBean.maxHeight = imageMaxHeight
        blockSpanBean.isFromDraft = isFromDraft
        if (TextUtils.isEmpty(realImagePath)) {
            LogUtil.e(TAG, "file path is empty")
            return
        }
        val imageFile = File(realImagePath)
        if (!imageFile.exists()) {
            LogUtil.e(TAG, "image file does not exist")
            return
        }
        when (val fileType: String = FileUtil.getFileType(realImagePath)) {
            FileTypeEnum.VIDEO -> blockSpanBean.isGif = false
            FileTypeEnum.STATIC_IMAGE, FileTypeEnum.GIF -> {
                blockSpanBean.isGif = FileTypeEnum.GIF == fileType
                // 通过uri或path调用的可以断定为相册图片或视频，有添加圆角的需求
                blockSpanBean.isPhoto = true
            }
        }
        richEditText.insertBlockImage(realImagePath, blockSpanBean) {
            //只处理删除
            if (it.spanTouchBean != null) {
                //图片和video都有删除图标,所以我们一次性处理
                if (it.blockSpanObtainObject.isShowDel && it.spanTouchBean.type == R.id.ivImageDel) {
                    //删除span
                    Log.i(TAG, "doAddBlockImageSpan: ${it.spanTouchBean}")
                    val editable = richEditText.editableText
                    Log.i(TAG, "doAddBlockImageSpan: editable===$editable==${editable.length}")
                    var spanStart: Int = editable.getSpanStart(it)
                    var spanEnd: Int = editable.getSpanEnd(it)
                    Log.i(
                        TAG,
                        "setMarkIconVisibility: blockImageSpan.spanStart==$spanStart"
                    )
                    Log.i(
                        TAG,
                        "setMarkIconVisibility: blockImageSpan.spanEnd==$spanEnd"
                    )
                    if (spanStart > 0) {
                        spanStart--; //光标跳到上一行
                    }
                    //表明图片后面没有文字
                    if (spanEnd + 1 == editable.length) {
                        spanEnd++
                    }
                    Log.i(
                        TAG,
                        "setMarkIconVisibility:2222 blockImageSpan.spanStart==$spanStart"
                    )
                    editable.delete(spanStart, spanEnd)
//                    editable.replace(spanStart, spanEnd, "")
                    Log.i(TAG, "doAddBlockImageSpan: 222editable==$editable")
                    richEditText.setSelection(spanStart)
                    /**
                     * 这里为了解决删除图片触发异常，java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0 at
                     * 触发异常是因为getOffsetForPosition(x,y)返回值是-1造成的。
                     * 所以这里我们尽量不要让它为-1即可。
                     *
                     *
                     */
                    Log.i(TAG, "doAddBlockImageSpan: richEditText==" + richEditText.layout)
                    richEditText.isDeletingImg = true
                    Log.i(
                        TAG,
                        "doAddBlockImageSpan: ri==" + richEditText.getOffsetForPosition(
                            it.spanTouchBean.x,
                            it.spanTouchBean.y
                        )
                    )
                    Handler().postDelayed({ richEditText.isDeletingImg = false }, 50)
                    Log.i(TAG, "doAddBlockImageSpan: 删除完成")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // 相册图片返回
            val selectedImageUri = data.data ?: return
            val realImagePath = FileUtil.getFileRealPath(this, selectedImageUri) ?: return
            Log.d(TAG, "realImagePath: $realImagePath")
            val fileType = FileUtil.getFileType(realImagePath) ?: return
            Log.i(TAG, "onActivityResult: fileType===$fileType")
            when (fileType) {
                FileTypeEnum.STATIC_IMAGE, FileTypeEnum.GIF -> {
                    val imageVm =
                        BlockSpanBean<Any>(
                            "2",
                            realImagePath
                        )
                    doAddBlockImageSpan(realImagePath, imageVm)
                }
                FileTypeEnum.VIDEO -> {
                    // 插入视频封面
                    doAddBlockImageSpan(
                        realImagePath, BlockSpanBean<Any>(
                            BlockSpanEnum.VIDEO,
                            "3",
                            realImagePath
                        ).apply {
                            spanObtainObject = VideoVm(realImagePath)
                        }
                    )
                }
            }
        }

        if (requestCode == AT_USER && resultCode == RESULT_OK && data != null) {

            data.run {
                val userId = getIntExtra("userId", -1)
                val userName = getStringExtra("userName")
                val content = "@$userName"
                richEditText.insertAt(
                    InlineSpanBean<Any>(
                        InlineSpanEnum.AT,
                        userId.toString(),
                        content,
                    )
                ) {
                    Log.i(TAG, "onActivityResult: 点击了@用户")
                }
            }
        }

        if (requestCode == TOPIC_CONTENT && resultCode == RESULT_OK && data != null) {
            // 相册图片返回
            data.run {
                val userId = getIntExtra("userId", -1)
                val userName = getStringExtra("userName")
                val content = "#$userName"
                richEditText.insertAt(
                    InlineSpanBean<Any>(
                        TOPIC,
                        userId.toString(),
                        content,
                    )
                ) {
                    Log.i(TAG, "onActivityResult: 点击了#话题")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        handleSaveDraft()
    }

}
