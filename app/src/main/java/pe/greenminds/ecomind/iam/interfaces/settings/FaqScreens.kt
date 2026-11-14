package pe.greenminds.ecomind.iam.interfaces.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SurfaceSoft

data class FaqItem(val id: Int, val category: String, val question: String, val answer: String)

val ecoMindFaqs = listOf(
    FaqItem(1, "Primeros pasos", "¿Qué es EcoMind?", "EcoMind convierte hábitos sostenibles en misiones, aprendizaje y recompensas para ayudarte a cuidar el planeta."),
    FaqItem(2, "Primeros pasos", "¿Cómo completo una misión?", "Entra a Misiones, selecciona una actividad, revisa sus instrucciones y completa cada objetivo. Al finalizar, confirma la misión para recibir tus EcoPoints."),
    FaqItem(3, "EcoPoints y ranking", "¿Cómo gano EcoPoints?", "Ganas EcoPoints completando misiones, actividades educativas y minijuegos. Prueba actividades diferentes para seguir aumentando tu puntuación."),
    FaqItem(4, "EcoPoints y ranking", "¿Quién aparece en el ranking?", "El ranking muestra participantes según el alcance y periodo elegidos: local, global, amistades o familias; diario, semanal, mensual o histórico."),
    FaqItem(5, "Gemas y tienda", "¿Cómo consigo gemas?", "Puedes obtener gemas mediante recompensas y paquetes disponibles en la tienda. Sirven para adquirir avatares, cosméticos y potenciadores."),
    FaqItem(6, "Familia y comunidad", "¿Cómo participo en un evento comunitario?", "Abre Comunidad, entra en Eventos, selecciona uno disponible y presiona Unirme. Allí podrás revisar sus detalles y participantes.")
)

@Composable
private fun SettingsPageHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(58.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(painterResource(R.drawable.ic_chevron_back), "Volver", tint = EcoGreen)
        }
        Text(title, Modifier.align(Alignment.Center), color = EcoGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit, onOpenFaq: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp)) {
        SettingsPageHeader("Ayuda y soporte", onBack)
        Spacer(Modifier.height(18.dp))
        Text("¿Cómo podemos ayudarte?", color = EcoGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenFaq), color = SurfaceSoft, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Preguntas frecuentes", fontSize = 16.sp)
                    Text("Respuestas organizadas por categoría", color = Color(0xFF7E8B75), fontSize = 12.sp)
                }
                Icon(painterResource(R.drawable.ic_chevron_back), null, Modifier.rotate(180f), tint = EcoGreen)
            }
        }
    }
}

@Composable
fun FaqScreen(onBack: () -> Unit, onOpenQuestion: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        SettingsPageHeader("Preguntas frecuentes", onBack)
        ecoMindFaqs.groupBy(FaqItem::category).forEach { (category, questions) ->
            Text(category, Modifier.padding(top = 18.dp, bottom = 8.dp), color = EcoGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Surface(Modifier.fillMaxWidth(), color = SurfaceSoft, shape = RoundedCornerShape(12.dp)) {
                Column {
                    questions.forEachIndexed { index, faq ->
                        Row(Modifier.fillMaxWidth().clickable { onOpenQuestion(faq.id) }.padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(faq.question, Modifier.weight(1f), fontSize = 14.sp)
                            Icon(painterResource(R.drawable.ic_chevron_back), null, Modifier.rotate(180f), tint = EcoGreen)
                        }
                        if (index != questions.lastIndex) HorizontalDivider(color = Color(0xFFE2E7DF))
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun FaqDetailScreen(questionId: Int, onBack: () -> Unit) {
    val faq = ecoMindFaqs.firstOrNull { it.id == questionId }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        SettingsPageHeader("Preguntas frecuentes", onBack)
        if (faq == null) {
            Text("No hay preguntas frecuentes disponibles", Modifier.padding(top = 32.dp), color = Color.Gray)
        } else {
            Text(faq.category, Modifier.padding(top = 22.dp), color = EcoGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(faq.question, Modifier.padding(top = 8.dp), fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)
            Text(faq.answer, Modifier.padding(top = 18.dp), color = Color(0xFF77836F), fontSize = 15.sp, lineHeight = 23.sp)
            Text("‹ Volver a las preguntas", Modifier.padding(top = 24.dp).clickable(onClick = onBack), color = EcoGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
