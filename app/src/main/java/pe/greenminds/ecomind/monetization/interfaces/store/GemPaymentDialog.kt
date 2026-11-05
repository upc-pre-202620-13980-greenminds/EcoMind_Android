package pe.greenminds.ecomind.monetization.interfaces.store

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.PaymentMethod
import java.util.Locale

private enum class CheckoutProvider { CULQI, PAYPAL }

@Composable
fun GemPaymentDialog(
    gemPackage: GemPackage,
    isProcessing: Boolean,
    errorMessage: String?,
    onPay: (PaymentMethod, sourceToken: String, email: String) -> Unit,
    onDismiss: () -> Unit
) {
    var provider by rememberSaveable { mutableStateOf<CheckoutProvider?>(null) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            shape = RoundedCornerShape(15.dp),
            color = Color.White,
            shadowElevation = 14.dp
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PaymentHeader(gemPackage, isProcessing, onDismiss)
                when {
                    isProcessing -> PaymentProcessing()
                    provider == null -> {
                        Text(
                            stringResource(R.string.store_payment_choose_method),
                            color = Color(0xFF777777), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PaymentMethodCard(
                                modifier = Modifier.weight(1f),
                                provider = CheckoutProvider.CULQI,
                                logoColor = Color(0xFFF2EEF8),
                                name = stringResource(R.string.store_payment_culqi),
                                description = stringResource(R.string.store_payment_culqi_desc)
                            ) { provider = CheckoutProvider.CULQI }
                            PaymentMethodCard(
                                modifier = Modifier.weight(1f),
                                provider = CheckoutProvider.PAYPAL,
                                logoColor = Color(0xFFEAF7FC),
                                name = stringResource(R.string.store_payment_paypal),
                                description = stringResource(R.string.store_payment_paypal_desc)
                            ) { provider = CheckoutProvider.PAYPAL }
                        }
                    }
                    else -> {
                        Text(
                            "‹ ${stringResource(R.string.store_payment_back)}",
                            modifier = Modifier.clickable { provider = null },
                            color = Color(0xFF7D7D7D),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp
                        )
                        PaymentCheckoutWebView(provider!!, gemPackage, onPay)
                    }
                }
                errorMessage?.let {
                    Surface(color = Color(0xFFFFEEEE), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(it, color = Color(0xFFC65A56), fontSize = 13.sp)
                            Text(
                                stringResource(R.string.store_payment_retry),
                                modifier = Modifier.clickable { provider = null },
                                color = Color(0xFF65A94D), fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHeader(gemPackage: GemPackage, locked: Boolean, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) { Text("💎", fontSize = 28.sp) }
        Column(Modifier.padding(start = 7.dp).weight(1f)) {
            Text(gemPackage.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF252525))
            Text("${String.format(Locale.US, "%,d", gemPackage.gemAmount)} gemas", color = Color(0xFF8D8D8D), fontSize = 10.sp)
        }
        Text(
            "S/${String.format(Locale.US, "%.2f", gemPackage.price)}",
            fontWeight = FontWeight.ExtraBold, color = Color(0xFF2196F3), fontSize = 15.sp
        )
        Text("×", modifier = Modifier.padding(start = 12.dp).clip(CircleShape).clickable(enabled = !locked) { onDismiss() }.padding(4.dp), fontSize = 20.sp, color = Color(0xFF777777))
    }
}

@Composable
private fun PaymentMethodCard(
    modifier: Modifier,
    provider: CheckoutProvider,
    logoColor: Color,
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD8D8D8)),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(width = 60.dp, height = 44.dp).clip(RoundedCornerShape(8.dp)).background(logoColor), contentAlignment = Alignment.Center) {
                PaymentProviderLogo(provider)
            }
            Text(name, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF242424), fontSize = 12.sp)
            Text(description, textAlign = TextAlign.Center, color = Color(0xFF9A9A9A), fontSize = 9.sp)
        }
    }
}

@Composable
private fun PaymentProviderLogo(provider: CheckoutProvider) {
    when (provider) {
        CheckoutProvider.CULQI -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(width = 31.dp, height = 15.dp)) {
                drawCircle(Color(0xFFEB001B), radius = 7.dp.toPx(), center = Offset(11.dp.toPx(), 7.5.dp.toPx()))
                drawCircle(Color(0xFFF79E1B), radius = 7.dp.toPx(), center = Offset(20.dp.toPx(), 7.5.dp.toPx()))
                drawCircle(Color(0xFFFF5F00), radius = 5.dp.toPx(), center = Offset(15.5.dp.toPx(), 7.5.dp.toPx()))
            }
            Text("yape", color = Color(0xFF742284), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, lineHeight = 11.sp)
        }
        CheckoutProvider.PAYPAL -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("P", color = Color(0xFF003087), fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("ay", color = Color(0xFF0070BA), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Text("Pal", color = Color(0xFF003087), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PaymentProcessing() {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CircularProgressIndicator(color = Color(0xFF65A94D))
        Text(stringResource(R.string.store_payment_processing), fontWeight = FontWeight.Bold, color = Color(0xFF555555))
        Text(stringResource(R.string.store_payment_processing_hint), color = Color(0xFF999999), fontSize = 12.sp)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PaymentCheckoutWebView(
    provider: CheckoutProvider,
    gemPackage: GemPackage,
    onPaymentToken: (PaymentMethod, String, String) -> Unit
) {
    val html = remember(provider, gemPackage) {
        when (provider) {
            CheckoutProvider.CULQI -> culqiCheckoutHtml(gemPackage)
            CheckoutProvider.PAYPAL -> paypalCheckoutHtml(gemPackage)
        }
    }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?, request: WebResourceRequest?
                    ): Boolean = handlePaymentUri(request?.url, onPaymentToken)

                    @Deprecated("Android 7 fallback")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                        handlePaymentUri(url?.let(Uri::parse), onPaymentToken)
                }
                loadDataWithBaseURL(
                    "https://checkout.ecomind.test",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = Modifier.fillMaxWidth().height(460.dp)
    )
}

private fun handlePaymentUri(
    uri: Uri?,
    onPaymentToken: (PaymentMethod, String, String) -> Unit
): Boolean {
    if (uri?.scheme != "ecomind-payment") return false
    if (uri.host == "success") {
        val method = runCatching {
            PaymentMethod.valueOf(uri.getQueryParameter("method").orEmpty().uppercase())
        }.getOrDefault(PaymentMethod.CARD)
        val token = uri.getQueryParameter("token").orEmpty()
        val email = uri.getQueryParameter("email").orEmpty()
        if (token.isNotBlank()) onPaymentToken(method, token, email)
    }
    return true
}

private fun culqiCheckoutHtml(gemPackage: GemPackage): String = """
    <!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
    <script src="https://checkout.culqi.com/js/v4"></script></head>
    <body style="font-family:sans-serif;text-align:center;padding:16px">
      <p>El checkout seguro de Culqi se abrirá automáticamente.</p>
      <button onclick="openCulqi()" style="padding:12px 24px">Abrir Culqi</button>
      <script>
        function openCulqi() {
          if (!window.Culqi) return;
          Culqi.publicKey='${BuildConfig.CULQI_PUBLIC_KEY}';
          Culqi.settings({title:'EcoMind',currency:'${gemPackage.currency}',amount:${(gemPackage.price * 100).toInt()}});
          Culqi.options({lang:'es',installments:false,paymentMethods:{tarjeta:true,yape:true,bancaMovil:false,agente:false,billetera:false,cuotealo:false}});
          Culqi.open();
        }
        function culqi() {
          if (Culqi.token) {
            const token=Culqi.token.id;
            const method=token.startsWith('ype_')?'YAPE':'CARD';
            const email=Culqi.token.email||'buyer@ecomind.com';
            location.href='ecomind-payment://success?method='+method+'&token='+encodeURIComponent(token)+'&email='+encodeURIComponent(email);
          }
        }
        window.onload=openCulqi;
      </script>
    </body></html>
""".trimIndent()

private fun paypalCheckoutHtml(gemPackage: GemPackage): String {
    val usdValue = String.format(Locale.US, "%.2f", gemPackage.price * 0.27)
    return """
        <!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
        <script src="https://www.paypal.com/sdk/js?client-id=${BuildConfig.PAYPAL_CLIENT_ID}&currency=USD&disable-funding=card"></script>
        </head><body style="font-family:sans-serif;padding:16px">
          <div id="paypal-button-container"></div>
          <script>
            paypal.Buttons({
              createOrder: function(data, actions) {
                return actions.order.create({purchase_units:[{amount:{currency_code:'USD',value:'$usdValue'}}]});
              },
              onApprove: function(data) {
                location.href='ecomind-payment://success?method=PAYPAL&token='+encodeURIComponent(data.orderID)+'&email='+encodeURIComponent('buyer@ecomind.com');
              }
            }).render('#paypal-button-container');
          </script>
        </body></html>
    """.trimIndent()
}

@Composable
fun GemPaymentSuccessDialog(gems: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp), shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 14.dp) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 28.sp)
                    Column(Modifier.padding(start = 7.dp).weight(1f)) {
                        Text(stringResource(R.string.store_payment_package_name), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text("${String.format(Locale.US, "%,d", gems)} gemas", color = Color(0xFF8D8D8D), fontSize = 10.sp)
                    }
                    Text("×", modifier = Modifier.clip(CircleShape).clickable(onClick = onDismiss).padding(4.dp), fontSize = 20.sp, color = Color(0xFF777777))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFE2F6DF)), contentAlignment = Alignment.Center) {
                        Text("✓", color = Color(0xFF58B54C), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Text(stringResource(R.string.store_payment_success_title), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF777777))
                }
                Text(stringResource(R.string.store_payment_success_message, gems), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF65A94D), fontSize = 10.sp)
                Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onDismiss), color = Color(0xFF65A94D), shape = RoundedCornerShape(20.dp)) {
                    Text(stringResource(R.string.store_payment_done), modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
