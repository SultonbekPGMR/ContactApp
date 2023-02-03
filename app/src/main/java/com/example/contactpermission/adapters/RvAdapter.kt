package com.example.contactpermission.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.example.contactpermission.databinding.RvItemBinding
import com.example.contactpermission.models.ContactType
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.contactpermission.R

class RvAdapter(val context: Context, var list: List<ContactType>, val rvClick: RvClick) :
    RecyclerSwipeAdapter<RvAdapter.Vh>() {

    inner class Vh(private val itemRvBinding: RvItemBinding) :
        RecyclerView.ViewHolder(itemRvBinding.root) {


        fun onBind(user: ContactType, position: Int) {

            /** default card values*/
            itemRvBinding.apply {
                tvName.text = user.name
                tvNumber.text = user.number
                tvCount.text = "${position + 1}"

                phoneCall.setOnClickListener {
                    rvClick.rvCallClicked(user)
                }

                itemCard.setOnClickListener {
                    rvClick.rvItemClicked(user, position)
                }


            }

            /** dealing with swipeLayout */
            itemRvBinding.apply {
                btnCall.setOnClickListener {
                    rvClick.rvCallClicked(user)
                }
                btnSms.setOnClickListener {
                    rvClick.rvSmsClicked(user)
                }


            }


        }
    }

    val  mItemManger =  SwipeItemRecyclerMangerImpl(this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: Vh, position: Int) {

        holder.onBind(list[position], position)
        mItemManger.bindView(holder.itemView, position);
    }


    override fun getItemCount(): Int = list.size
    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }
}

interface RvClick {
    fun rvItemClicked(user: ContactType, position: Int)
    fun rvCallClicked(user: ContactType)
    fun rvSmsClicked(user: ContactType)
}