package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Educational content shown below Giovanna's calendar.
 *
 * The information is intentionally descriptive: calendar calculations estimate
 * phases and never confirm ovulation or provide contraceptive protection.
 */
@Composable
fun CycleEducationCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Um pouco mais sobre seu ciclo 🌷",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Informações para conhecer seu corpo, observar padrões e conversar com sua ginecologista.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EducationSection(
                title = "As fases do ciclo",
                body = "O ciclo começa no primeiro dia do sangramento. Na fase folicular, que inclui a menstruação, os folículos ovarianos se desenvolvem e o estrogênio tende a aumentar. Na ovulação, um ovário libera um óvulo. Depois, na fase lútea, a progesterona aumenta; se não houver gestação, os hormônios caem e um novo sangramento pode começar."
            )

            EducationSection(
                title = "Janela fértil: o que significa?",
                body = "A janela fértil corresponde aproximadamente aos cinco dias anteriores à ovulação e ao próprio dia da ovulação. Isso ocorre porque os espermatozoides podem permanecer viáveis por alguns dias e o óvulo costuma permanecer viável por cerca de 12 a 24 horas. A duração e o momento dessa janela variam entre ciclos."
            )

            EducationSection(
                title = "Sinais que podem acompanhar a ovulação",
                body = "Algumas pessoas observam muco cervical mais transparente, escorregadio e elástico, semelhante à clara de ovo. Também podem ocorrer alterações discretas na temperatura basal após a ovulação. Testes urinários de LH identificam o pico hormonal que geralmente antecede a ovulação, mas nenhum sinal isolado garante que ela ocorreu."
            )

            EducationSection(
                title = "O que vale registrar no calendário",
                body = "Além do primeiro e do último dia do sangramento, anote intensidade do fluxo, cólicas, dor, humor, energia, sono, sintomas e mudanças no muco cervical. Registros consistentes ao longo de vários ciclos ajudam a reconhecer o padrão individual e fornecem informações úteis em uma consulta."
            )

            EducationSection(
                title = "Variações são esperadas",
                body = "Nem todo ciclo dura 28 dias e a ovulação não acontece necessariamente no 14º dia. Estimativas baseadas apenas em datas anteriores podem errar, inclusive em ciclos aparentemente regulares. Por isso, as marcações férteis e de ovulação deste calendário são previsões aproximadas, não confirmações biológicas."
            )

            EducationSection(
                title = "Quando procurar orientação",
                body = "Converse com uma profissional de saúde se houver mudanças persistentes no padrão, sangramento muito intenso, dor que atrapalha as atividades, sangramento entre menstruações ou ausência menstrual sem explicação. Dor súbita ou intensa, desmaio ou sangramento acompanhado de fraqueza importante exigem avaliação urgente."
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Referências para leitura",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                ReferenceLine("ACOG. Menstruation as a vital sign (Committee Opinion No. 651).")
                ReferenceLine("ACOG. Fertility Awareness-Based Methods of Family Planning.")
                ReferenceLine("ASRM. Optimizing Natural Fertility: a committee opinion (2022).")
                ReferenceLine("ASRM. Fertility Evaluation of Infertile Women: a committee opinion (2026).")
                ReferenceLine("NICE. Fertility problems: assessment and treatment (NG257, 2026).")
            }

            Text(
                text = "Este conteúdo é educativo e não substitui avaliação médica. O calendário não deve ser usado para evitar gravidez nem para decidir, sozinho, se houve ovulação.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EducationSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReferenceLine(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
