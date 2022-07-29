package com.yuruiyin.richeditor.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yuruiyin.richeditor.sample.model.User
import kotlinx.android.synthetic.main.activity_at_list.*

class AtListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_at_list)
        val userList = mutableListOf<User>()
        for (i in 0..30) {
            userList.add(User(i, "昵称$i"))
        }
        val atListAdapter = AtListAdapter(this, userList).also {
            rvUserList.adapter = it
        }

        rvUserList.setOnItemClickListener { _, _, position, id ->
            val user: User = atListAdapter.getItem(position)
            val intent = Intent().apply {
                putExtra("userId", user.id)
                putExtra("userName", user.name)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    internal class AtListAdapter(private val context: Context, data: MutableList<User>) :
        BaseAdapter() {
        private val mData: List<User> = data
        private val mLayoutInflater: LayoutInflater by lazy {
            LayoutInflater.from(context)
        }


        override fun getCount(): Int {
            return mData.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): User {
            return mData[position]
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return mLayoutInflater.inflate(R.layout.item_at, parent, false).also {
                val textView: TextView = it.findViewById(R.id.tvAtUserName)
                val item: User = getItem(position)
                textView.text = item.name
            }
        }
    }

}