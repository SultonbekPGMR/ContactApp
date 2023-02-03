package com.example.contactpermission.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.contactpermission.R
import com.example.contactpermission.adapters.RvAdapter
import com.example.contactpermission.adapters.RvClick
import com.example.contactpermission.databases.MyDbHelper
import com.example.contactpermission.databinding.FragmentHomeBinding
import com.example.contactpermission.models.ContactType
import com.github.florent37.runtimepermission.kotlin.askPermission
import java.util.*


class HomeFragment : Fragment(), RvClick {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var rvAdapter: RvAdapter
    private lateinit var myDbHelper: MyDbHelper
    private lateinit var contactList: ArrayList<ContactType>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        myDbHelper = MyDbHelper(requireContext())
        contactList = myDbHelper.getAllContacts()

        /** checking db empty or not */
        if (contactList.isEmpty()) askContactPermission()

        rvAdapter = RvAdapter(requireContext(), contactList, this)
        binding.myRv.adapter = rvAdapter


        /** searchView in toolBar */
        val listNames = contactList
        val searchView: SearchView = binding.toolbar.menu.getItem(0).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText.trim().isEmpty()) {
                    resetSearch();
                    return false;
                }


                val filteredValues = ArrayList(contactList)
                for (value in listNames) {
                    if (!value.name?.lowercase(Locale.getDefault())
                            ?.contains(newText.lowercase())!! && !value.number?.lowercase(Locale.getDefault())
                            ?.contains(newText.lowercase())!!
                    ) {
                        filteredValues.remove(value)
                    }
                }
                rvAdapter = RvAdapter(requireContext(), filteredValues, this@HomeFragment)
                binding.myRv.adapter = rvAdapter
                return false
            }
        })


        /** scrolling to last item(contact) */
//             if (IS_ADDED) {
//                 IS_ADDED = false
//                 binding.myRv.postDelayed({
//                     binding.myRv.scrollToPosition(rvAdapter.itemCount-1)
//                 }, 1000)
//             }

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.addFragment)
        }


        return binding.root
    }

    fun resetSearch() {
        rvAdapter = RvAdapter(requireContext(), contactList, this)
        binding.myRv.adapter = rvAdapter
    }

    private fun askContactPermission() {
        askPermission(
            android.Manifest.permission.READ_CONTACTS,
        ) {

            //all permissions already granted or just granted
            binding.progressBar.visibility = View.VISIBLE
            Thread {
                // do the thing that takes a long time
                contactList = readContactFromPhone() as ArrayList<ContactType>
                rvAdapter.list = contactList
                activity?.runOnUiThread {
                    rvAdapter.notifyItemRangeInserted(0, contactList.size)
                    binding.progressBar.visibility = View.INVISIBLE
                }
                AsyncTask.execute {
                    loadContactsToDb()

                }
            }.start()


        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(requireContext()).setMessage("Please accept our permissions")
                    .setPositiveButton("ok") { _, _ ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        Toast.makeText(context, "Goodbye", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                        dialog.dismiss()
                    }.show()
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                // you need to open setting manually if you really need it
                e.goToSettings()
            }
        }
    }


    private fun readContactFromPhone(): List<ContactType> {

        val contactsList = ArrayList<ContactType>()
        val contacts = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null, null
        )!!
        if (contacts.moveToFirst()) {
            do {
                val index1 =
                    contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val index2 = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contactsList.add(
                    ContactType(
                        contacts.getString(index1), "", contacts.getString(index2)
                    )
                )
            } while (contacts.moveToNext())
        }
        contacts.close()
        contactsList.sortWith((compareBy { it.name }))


        return contactsList
    }


    private fun loadContactsToDb() {
        contactList.forEach {
            myDbHelper.addContact(it)
        }
    }

    override fun rvItemClicked(user: ContactType, position: Int) {
        findNavController().navigate(
            R.id.viewUser, bundleOf("user" to user, "position" to position)
        )
    }

    override fun rvCallClicked(user: ContactType) {
        askCallPermission(user)

    }

    override fun rvSmsClicked(user: ContactType) {
        findNavController().navigate(R.id.sendSms, bundleOf("user" to user))
    }

    private fun askCallPermission(user: ContactType) {
        askPermission(
            android.Manifest.permission.CALL_PHONE
        ) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:${user.number}")
            startActivity(callIntent)
        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(requireContext()).setMessage("Please accept our permissions")
                    .setPositiveButton("ok") { _, _ ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss()
                    }.show()
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                // you need to open setting manually if you really need it
                e.goToSettings()
            }
        }
    }

    private fun getCallDetails(): String {
        val sb = StringBuffer()
        val managedCursor: Cursor = requireActivity().managedQuery(
            CallLog.Calls.CONTENT_URI, null, null, null, null
        )
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        sb.append("Call Details :")
        while (managedCursor.moveToNext()) {
            val phNumber = managedCursor.getString(number)
            val callType = managedCursor.getString(type)
            val callDate = managedCursor.getString(date)
            val callDayTime = Date(java.lang.Long.valueOf(callDate))
            val callDuration = managedCursor.getString(duration)
            var dir: String? = null
            val dircode = callType.toInt()
            when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            sb.append(
                """
Phone Number:--- $phNumber 
Call Type:--- $dir 
Call Date:--- $callDayTime 
Call duration in sec :--- $callDuration"""
            )
            sb.append("\n----------------------------------")
        }
        managedCursor.close()
        return sb.toString()
    }
}