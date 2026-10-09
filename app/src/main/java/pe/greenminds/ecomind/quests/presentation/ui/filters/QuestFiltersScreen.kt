package pe.greenminds.ecomind.quests.presentation.ui.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.QuestFilters
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen

@Composable
fun QuestFiltersContent(initial: QuestFilters, onApply: (QuestFilters) -> Unit, modifier: Modifier = Modifier) {
    var draftJson by rememberSaveable { mutableStateOf(Gson().toJson(initial)) }
    val draft = Gson().fromJson(draftJson, QuestFilters::class.java)
    fun update(values: Set<String>, key: String): Set<String> = if (key in values) values - key else values + key
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(2.dp))
            FilterSection(stringResource(R.string.quest_filter_category), listOf(
                "ENERGY" to stringResource(R.string.quest_category_energy), "WATER" to stringResource(R.string.quest_category_water),
                "RECYCLE" to stringResource(R.string.quest_category_recycle)), draft.categories) {
                draftJson = Gson().toJson(draft.copy(categories = update(draft.categories, it)))
            }
            FilterSection(stringResource(R.string.quest_filter_type), listOf(
                "COLLABORATIVE" to stringResource(R.string.quest_theme_collaborative), "MINIGAME" to stringResource(R.string.quest_theme_minigame),
                "CHECKBOX" to stringResource(R.string.quest_theme_checkbox), "WRITE" to stringResource(R.string.quest_filter_write)), draft.types, true) {
                draftJson = Gson().toJson(draft.copy(types = update(draft.types, it)))
            }
            FilterSection(stringResource(R.string.quest_filter_age), listOf(
                "6:9" to stringResource(R.string.quest_filter_age_6_9), "10:13" to stringResource(R.string.quest_filter_age_10_13),
                "14:17" to stringResource(R.string.quest_filter_age_14_17), "18:2147483647" to stringResource(R.string.quest_filter_age_18)), draft.ages) {
                draftJson = Gson().toJson(draft.copy(ages = update(draft.ages, it)))
            }
            FilterSection(stringResource(R.string.quest_filter_time), listOf(
                "0:15" to stringResource(R.string.quest_filter_time_15), "16:30" to stringResource(R.string.quest_filter_time_30),
                "31:60" to stringResource(R.string.quest_filter_time_60), "61:2147483647" to stringResource(R.string.quest_filter_time_long)), draft.times) {
                draftJson = Gson().toJson(draft.copy(times = update(draft.times, it)))
            }
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = { onApply(draft) }, modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp).height(48.dp),
            shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)) {
            Text(stringResource(R.string.quest_filter_apply), fontSize = 16.sp)
        }
    }
}

@Composable
private fun FilterSection(title: String, options: List<Pair<String, String>>, selected: Set<String>, initiallyOpen: Boolean = false, onToggle: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(initiallyOpen) }
    Surface(shape = RoundedCornerShape(5.dp), shadowElevation = 5.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { expanded = !expanded }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(if (expanded) "⌃" else "⌄", color = Color.Gray, fontSize = 22.sp)
            }
            if (expanded) Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                options.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth().toggleable(key in selected, role = Role.Checkbox, onValueChange = { onToggle(key) }), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = key in selected, onCheckedChange = null, modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp).size(18.dp), colors = CheckboxDefaults.colors(checkedColor = EcoGreen))
                        Text(label.replaceFirstChar(Char::uppercase), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

