package com.example.myapplicationdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class MyFragment : Fragment() {
    val TAG ="MyFragment"
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MyFragment: onCreate")
    }
    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "MyFragment: onCreateView")
        return inflater.inflate(R.layout.fragment_my, container,false)
    }
    override fun onViewCreated(view:View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MyFragment: onViewCreated")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d (TAG, "MyFragment: onDestroyView")
    }
}
