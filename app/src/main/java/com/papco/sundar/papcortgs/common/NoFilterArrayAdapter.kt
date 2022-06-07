package com.papco.sundar.papcortgs.common

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter<T>(context: Context, resource: Int, textViewId: Int, private val objects: List<T>)
    : ArrayAdapter<T>(context, resource, textViewId, objects) {


    override fun getFilter(): Filter {
        return object:Filter(){

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values=objects
                    count=objects.count()
                }

            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
               if(results!=null && results.count > 0)
                   notifyDataSetChanged()
                else
                   notifyDataSetInvalidated()
            }


        }
    }
}