package com.example.nexus


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object NewsList : Screen("news_list")
    object Favorites : Screen("favorites")
    object About : Screen("about")
    object ArticleDetail : Screen("article_detail/{articleId}") {
        fun passId(id: String): String {
            val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
            return "article_detail/$encodedId"
        }
    }
}

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.NewsList.route,
        modifier = modifier
    ) {
        composable(Screen.NewsList.route) {
            NewsListScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.passId(articleId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.passId(articleId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) {
            ArticleDetailScreen()
        }
    }
}
