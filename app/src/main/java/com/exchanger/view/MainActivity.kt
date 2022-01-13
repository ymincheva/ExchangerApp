package com.exchanger.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.exchanger.R
import com.exchanger.databinding.ActivityMainBinding
import com.exchanger.helper.EndPoints
import com.exchanger.helper.Resource
import com.exchanger.helper.Utility
import com.exchanger.utils.Const
import com.exchanger.utils.Utils
import com.exchanger.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()
    //private var totalCount: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        initSpinner()

        setUpClickListener()
    }

    private fun setUpClickListener() {
        binding.btnConvert.setOnClickListener {

            val accountBalance = binding.accountBalanceTxt.text.toString()
            val amountToConvert = binding.amountEditText.text.toString()

            if (amountToConvert.isEmpty() || amountToConvert == "0") {
                Snackbar.make(
                    binding.mainLayout,
                    "Please enter an amount to convert",
                    Snackbar.LENGTH_LONG
                )
                    .withColor(ContextCompat.getColor(this, R.color.red))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
            } else if (accountBalance.isEmpty() || accountBalance == "0") {
                Snackbar.make(
                    binding.mainLayout,
                    "Please enter an account balance",
                    Snackbar.LENGTH_LONG
                )
                    .withColor(ContextCompat.getColor(this, R.color.red))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
            }
            //check if internet is available
            else if (!Utility.isNetworkAvailable(this)) {
                Snackbar.make(
                    binding.mainLayout,
                    "You are not connected to the internet",
                    Snackbar.LENGTH_LONG
                )
                    .withColor(ContextCompat.getColor(this, R.color.red))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
            } else {
                doConversion()
            }
        }
    }


    private fun doConversion() {
        //hide keyboard
        Utility.hideKeyboard(this)

        //make progress bar visible
        binding.prgLoading.visibility = View.VISIBLE

        //make button invisible
        binding.btnConvert.visibility = View.GONE

        //Get the data inputed
        val apiKey = EndPoints.API_KEY

        setCounter()

        //do the conversion
        mainViewModel.getConvertedData(apiKey)

        //observe for changes in UI
        observeUi()
    }

    @SuppressLint("SetTextI18n")
    private fun observeUi() {
        mainViewModel.data.observe(this, androidx.lifecycle.Observer { result ->

            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (result.data?.success!!) {

                        val map: Map<String, Double>
                        map = result.data.rates

                        val sellCurrenciesSpinner =
                            findViewById<Spinner>(R.id.sellCurrenciesSpinner)
                        val receiveCurrenciesSpinner =
                            findViewById<Spinner>(R.id.receiveCurrenciesSpinner)

                        val rate = map.getValue(receiveCurrenciesSpinner.selectedItem.toString())
                        var amount = binding.amountEditText.text.toString().toDouble()
                        var accountBalance = binding.accountBalanceTxt.text.toString().toDouble()

                        if (!sellCurrenciesSpinner.selectedItem.toString().equals(Const.EUR)) {
                            val rateSell =
                                map.getValue(sellCurrenciesSpinner.selectedItem.toString())
                            amount = amount / rateSell
                        }
                        if (amount > accountBalance) {
                            Snackbar.make(
                                binding.mainLayout,
                                "Please enter a smaller amount to convert.",
                                Snackbar.LENGTH_LONG
                            )
                                .withColor(ContextCompat.getColor(this, R.color.red))
                                .setTextColor(ContextCompat.getColor(this, R.color.white))
                                .show()

                            binding.amountEditText.text.clear()
                            binding.prgLoading.visibility = View.GONE
                            binding.btnConvert.visibility = View.VISIBLE
                            return@Observer
                        }

                        var commission_fee: Double = 0.0
                        var counter = getCounter()
                        if (counter > Const.COUNTER) commission_fee = Const.COMMISSION_FEE

                        val formattedString =
                            String.format("%,.2f", (amount * rate!! - commission_fee))

                        binding.receiveAmountEditText.setText(formattedString)

                        infoDialog(
                            binding.amountEditText.text.toString().toDouble(),
                            String.format("%,.2f", (amount * rate!!)).toDouble(),
                            commission_fee
                        )
                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE

                    } else {
                        val layout = binding.mainLayout
                        Snackbar.make(
                            layout,
                            "Ooops! something went wrong, Try again",
                            Snackbar.LENGTH_LONG
                        )
                            .withColor(ContextCompat.getColor(this, R.color.red))
                            .setTextColor(ContextCompat.getColor(this, R.color.white))
                            .show()

                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE
                    }
                }
                Resource.Status.ERROR -> {
                    val layout = binding.mainLayout
                    Snackbar.make(
                        layout,
                        "Oopps! Something went wrong, Try again",
                        Snackbar.LENGTH_LONG
                    )
                        .withColor(ContextCompat.getColor(this, R.color.red))
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                    //stop progress bar
                    binding.prgLoading.visibility = View.GONE
                    //show button
                    binding.btnConvert.visibility = View.VISIBLE
                }

                Resource.Status.LOADING -> {
                    //stop progress bar
                    binding.prgLoading.visibility = View.VISIBLE
                    //show button
                    binding.btnConvert.visibility = View.GONE
                }
            }
        })
    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }

    @SuppressLint("StringFormatMatches")
    private fun infoDialog(amount_from: Double, amount_to: Double, tax: Double) {
        var text: String = this.resources.getString(
            R.string.info_message, amount_from, amount_to, tax
        )
        Snackbar.make(
            binding.mainLayout,
            text,
            Snackbar.LENGTH_LONG
        )
            .withColor(ContextCompat.getColor(this, R.color.white))
            .setTextColor(ContextCompat.getColor(this, R.color.black))
            .setActionTextColor(ContextCompat.getColor(this, R.color.blue))
            .setAction(
                this.resources.getString(
                    R.string.done
                )
            ) {
            }
            .show()
    }

    private fun setCounter() {
        var totalCount: Int = Utils(this).getPreferences("counter")
        totalCount = totalCount!! + 1
        Utils(this).putPreferences("counter", totalCount!!)
    }

    private fun getCounter(): Int {
        return Utils(this).getPreferences("counter")
    }

    private fun initSpinner() {
        val currencies = resources.getStringArray(R.array.currencies)

        val sellCurrenciesSpinner = findViewById<Spinner>(R.id.sellCurrenciesSpinner)
        if (sellCurrenciesSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, currencies
            )
            sellCurrenciesSpinner.adapter = adapter

            sellCurrenciesSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        val receiveCurrenciesSpinner = findViewById<Spinner>(R.id.receiveCurrenciesSpinner)
        if (receiveCurrenciesSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, currencies
            )
            receiveCurrenciesSpinner.adapter = adapter

            receiveCurrenciesSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }
}