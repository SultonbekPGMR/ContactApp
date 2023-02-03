package com.example.contactpermission.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.contactpermission.R
import com.example.contactpermission.databases.MyDbHelper
import com.example.contactpermission.databases.MyObject
import com.example.contactpermission.databinding.FragmentViewUserBinding
import com.example.contactpermission.models.ContactType
import java.lang.Long
import java.util.*
import kotlin.Array
import kotlin.Int
import kotlin.String
import kotlin.apply
import kotlin.getValue
import kotlin.lazy
import kotlin.toString


class ViewUser : Fragment() {


    private val binding by lazy { FragmentViewUserBinding.inflate(layoutInflater) }
    private lateinit var user: ContactType
    private lateinit var myDbHelper: MyDbHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /** getting arguments */
        user = arguments?.getSerializable("user") as ContactType
        myDbHelper = MyDbHelper(requireContext())
        val position = arguments?.getInt("position")
        binding.toolbar.menu.findItem(R.id.menu_save).isVisible = false



        /** toolbar icons */
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete -> {
                    deleteUser()
                }
                R.id.menu_edit -> {
                    binding.toolbar.menu.apply {
                        setGroupVisible(R.id.menu_group_view, false)
                        findItem(R.id.menu_save).isVisible = true
                    }
                    editUser()

                }
                R.id.menu_share -> {
                    shareUser()
                }

            }
            true
        }

        /** toolbar back icon*/
        binding.toolbar.setNavigationOnClickListener {
            if (binding.btnCall.isEnabled) {
                binding.apply {
                    viewLayout.slideDown(200)
                }
                Handler().postDelayed({
                    findNavController().popBackStack()
                }, 180)
            } else {
                closeEditMode()
            }

        }

        /** sliding up views (animation) */
        Handler().postDelayed(Runnable {
            binding.apply {
                linear.slideUp(300)
                tvLetter.slideUp(300)
                tvName.slideUp(300)
            }
        }, 45)

        /** getting clipBoardManager */
        val myClipboard: ClipboardManager =
            activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        binding.apply {
            tvName.text = "${user.name} ${user.surname}"
            tvNumber.text = user.number
            tvLetter.text = user.name?.get(0).toString().uppercase()


            // copying number to clipboard
            tvNumber.setOnLongClickListener {
                val myClip = ClipData.newPlainText("Number", user.number)
                myClipboard.setPrimaryClip(myClip)
                true
            }

            // PHONE CALL
            btnCall.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:${user.number}")
                startActivity(callIntent)
            }

            // SMS
            btnSms.setOnClickListener {
                val smsUri = Uri.parse("smsto:${user.number}")
                val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
                smsIntent.putExtra("sms_body", "hello..")
                startActivity(smsIntent)
            }


        }


        return binding.root
    }


    private fun deleteUser() {
        val dialog: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MyMenuDialogTheme)
                .setMessage("Do you really want to delete this contact")
                .setNegativeButton("No", null)
                .setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    myDbHelper.deleteContact(user)
                    Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        dialog.show()
    }

    private fun editUser() {
        binding.apply {
            btnCall.isEnabled = false
            btnSms.isEnabled = false
            toolbar.title = "Edit"
            viewLayout.slideDown(500)
            editLayout.slideDownEdit(500)
            val numberr = user.number!!.filter { !it.isWhitespace() }
            edtName.setText(user.name)
            edtNumber.setText(
                numberr.substring(numberr.length - 9)
            )

            binding.toolbar.menu.findItem(R.id.menu_save).setOnMenuItemClickListener {
                val name = edtName.text.toString().trim()
                val surname = edtSurname.text.toString().trim()
                val number = edtNumber.text.toString().trim()
                if (name.isEmpty()) {
                    edtName.error = "invalid name"
                } else if (number.length < 9 || !(MyObject.UZB_PHONE_NUM_PREFIX.contains(
                        number.substring(0, 2).toInt()
                    ))
                ) {
                    edtNumber.error = "invalid number"
                } else {
                    user.name = name
                    user.surname = surname
                    user.number = number
                    myDbHelper.editContact(user)
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    closeEditMode()

                }

                true
            }



        }

    }

    private fun shareUser() {
        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "sample")
        intent.putExtra(Intent.EXTRA_TEXT, "${user.name}: ${user.number}")

        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun closeEditMode() {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        binding.apply {
            viewLayout.slideUp(500)
            editLayout.slideUpEdit(500)
            binding.toolbar.menu.apply {
                setGroupVisible(R.id.menu_group_view, true)
                findItem(R.id.menu_save).isVisible = false
            }
            toolbar.title = "About"
            btnCall.isEnabled = true
            btnSms.isEnabled = true
            tvLetter.text = user.name?.get(0).toString().uppercase()
            tvName.text = user.name
            tvNumber.text = user.number
        }
    }

    private fun View.slideUp(duration: Int = 500) {
        visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, (this.height + 20).toFloat(), 0f)
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private fun View.slideDown(duration: Int = 500) {
        visibility = View.INVISIBLE
        val animate = TranslateAnimation(0f, 0f, 0f, (this.height + 20).toFloat())
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private fun View.slideDownEdit(duration: Int = 500) {
        visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, -(this.height).toFloat(), 0f)
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }

    private fun View.slideUpEdit(duration: Int = 500) {
        visibility = View.INVISIBLE
        val animate = TranslateAnimation(0f, 0f, 0f, -(this.height).toFloat())
        animate.duration = duration.toLong()
        animate.fillAfter = true
        this.startAnimation(animate)
    }




}