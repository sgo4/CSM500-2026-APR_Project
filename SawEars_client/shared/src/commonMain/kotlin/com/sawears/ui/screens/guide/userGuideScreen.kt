package com.sawears.ui.screens.guide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.sawears.ui.screens.components.ContentWithTopBar
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.userguide_page_title

/**
 * Shows instructions to the user
 *
 * @param locale device locale (e.g. en, ja)
 * @param onClickHome home button callback
 * @param onClickInfo info button callback
 */
@Composable
fun UserGuideScreen(
    locale: String,
    onClickHome: () -> Unit,
    onClickInfo: () -> Unit,
    onClickSettings: () -> Unit
){
    ContentWithTopBar(
        content = { innerPadding ->

            var guide by remember(locale) {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(locale) {
                guide = null
                guide = loadInstructions(locale)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    if (guide == null) {
                        CircularProgressIndicator()
                    } else {
                        Markdown(content=guide!!)
                    }
                }
            }
        },
        title = stringResource(Res.string.userguide_page_title),
        onClickHome = onClickHome,
        onClickInfo = onClickInfo,
        onClickSettings = onClickSettings
    )
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadInstructions(locale: String): String {
    val language = when (locale) {
        "ja" -> "ja"
        else -> "en"
    }

    return Res.readBytes(
        "files/$language/userguide.md"
    ).decodeToString()
}

@Preview
@Composable
fun UserGuideScreenPreview(){
    UserGuideScreen("en",{},{},{})
}