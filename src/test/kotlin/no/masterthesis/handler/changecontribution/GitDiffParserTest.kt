package no.masterthesis.handler.changecontribution

import no.masterthesis.handler.changecontribution.GitDiffParser.countLinesChanged
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class GitDiffParserTest{
  @ParameterizedTest(name = "{0} should have {2} lines added and {3} lines removed")
  @MethodSource("provideGitDiff")
  fun `'countLinesAdded' correctly counts lines`(fileName: String, diff: String, expectedLinesAdded: Int, expectedLinesRemoved: Int) {
    val (linesAdded, linesRemoved) = countLinesChanged(GitlabGitCommitDiff(
      oldPath = fileName,
      newPath = fileName,
      aMode = "100644",
      bMode = "100644",
      isFileRenamed = false,
      isFileDeleted = false,
      isNewFile = false,
      diff = diff,
    ))

    expectThat(linesAdded).isEqualTo(expectedLinesAdded)
    expectThat(linesRemoved).isEqualTo(expectedLinesRemoved)
  }

  /**
   * Moved files in GitLab are flagged with isFileRenamed, where oldPath and newPath is different,
   * whilst diff is only an empty string "".
   * */
  @Test
  fun `'countLinesAdded' handles moved files`() {
    val (linesAdded, linesRemoved) = countLinesChanged(GitlabGitCommitDiff(
      oldPath = "cypress.json",
      newPath = "models/cypress.json",
      aMode = "100644",
      bMode = "100644",
      isFileRenamed = true,
      isFileDeleted = false,
      isNewFile = false,
      diff = "",
    ))

    expectThat(linesAdded).isEqualTo(0)
    expectThat(linesRemoved).isEqualTo(0)
  }

  companion object {
    @JvmStatic
    fun provideGitDiff(): List<Arguments> = listOf(
      Arguments.of(
        // The first argument is just used for labelling of the test
        "client/src/pages/HomePage.js",
        "@@ -13,18 +13,28 @@ import Sorting from '../components/Sorting';\n import AlertBar from '../components/AlertBar';\n import LoadingSpinner from '../components/LoadingSpinner/LoadingSpinner';\n \n+/**\n+ * list of selectable media types to filter on\n+ */\n const toggleButtons = [\n   { label: 'All', value: null },\n   { label: 'Movies', value: 'movie' },\n   { label: 'TV Shows', value: 'series' },\n ];\n \n+/**\n+ * list of the different fields you can sort with\n+ */\n const sortOptions = [\n   { label: 'Rating', value: 'rating' },\n   { label: 'Released', value: 'released' },\n   { label: 'Name', value: 'name' },\n ];\n \n+/**\n+ * Responsible for rendering search view and search results.\n+ * It's the landing page for the application.\n+ */\n export class HomePage extends React.Component {\n   static propTypes = {\n     searchMedia: PropTypes.func.isRequired,\n@@ -63,12 +73,16 @@ export class HomePage extends React.Component {\n     hasSearched: PropTypes.bool.isRequired,\n   };\n \n+  /**\n+   * Checks parameters if there has occured an error after\n+   * a search is submitted and the loading is completed.\n+   */\n   receivedError = () => {\n     const { error, hasSearched, loading } = this.props;\n     return error !== null && hasSearched && !loading;\n   };\n \n-  receivedNoError = () => {\n+  hasLoadedSuccessfully = () => {\n     const { error, hasSearched, loading } = this.props;\n     return error === null && hasSearched && !loading;\n   };\n@@ -89,6 +103,9 @@ export class HomePage extends React.Component {\n     this.props.searchMedia(newQuery, type, limit, 0, sortField, sortDirection);\n   };\n \n+  /**\n+   * Update redux state and submit a search with a new type parameter\n+   */\n   onToggle = (newType) => {\n     const {\n       limit, query, sortField, sortDirection,\n@@ -101,6 +118,9 @@ export class HomePage extends React.Component {\n     }\n   };\n \n+  /**\n+   * Update redux state and submit a search with a new sortField parameters\n+   */\n   onSort = (newSortField) => {\n     const {\n       limit, query, type, sortDirection,\n@@ -111,6 +131,9 @@ export class HomePage extends React.Component {\n     this.props.searchMedia(query, type, limit, 0, newSortField, sortDirection);\n   };\n \n+  /**\n+   * Update redux state and submit a search with a new sortDirection parameter\n+   */\n   onDirectionClick = (newSortDirection) => {\n     const {\n       limit, query, type, sortField,\n@@ -121,7 +144,7 @@ export class HomePage extends React.Component {\n   };\n \n   /**\n-   * Updates the search fields with a new offset value\n+   * Update redux state and submit a search with a new offset parameter\n    * */\n   doPagination = (newOffset) => {\n     const {\n@@ -164,7 +187,7 @@ export class HomePage extends React.Component {\n         </div>\n         {this.receivedError() && <AlertBar message={error}/>}\n \n-        {this.receivedNoError() && <Sorting\n+        {this.hasLoadedSuccessfully() && <Sorting\n           directionValue={sortDirection}\n           fieldValue={sortField}\n           onDirectionClick={this.onDirectionClick}\n@@ -172,7 +195,7 @@ export class HomePage extends React.Component {\n           sortingMethods={sortOptions}/>\n         }\n         {loading && <LoadingSpinner/>}\n-        {this.receivedNoError() && <CoverDisplay\n+        {this.hasLoadedSuccessfully() && <CoverDisplay\n             media={allMedia}\n             url='/media/'\n             pagination={\n",
        // Expected Lines Added
        27,
        // Expected Lines Removed
        4,
      ),
      Arguments.of(
        // The first argument is just used for labelling of the test
        "client/src/components/CoverDisplay/CoverImage.js",
        "@@ -5,21 +5,34 @@ import defaultImage from '../InformationSection/no-image-found.jpg';\n \n import './CoverImage.less';\n \n-const getRating = (rating) => {\n-  if (typeof rating !== 'undefined') {\n-    return <em className='cover__overlay__rating'>{rating}/10</em>;\n+/**\n+ * Return an em tag with rating if rating is passed as prop to CoverImage\n+ */\n+const getRating = (rating = null) => {\n+  if (!rating) {\n+    return (null);\n   }\n-  return (null);\n+  return <em className='cover__overlay__rating'>⭐️: {rating}/10</em>;\n };\n \n-const getTitleAndYear = (title, released) => {\n-  if (typeof released === 'undefined') {\n+/**\n+ * Return an strong tag with the a title\n+ * If released is passed as a prop to CoverImage it will return title(released)\n+ */\n+const getTitleAndYear = (title, released = null) => {\n+  // If no release year is passed, don't render release year\n+  if (!released) {\n     return <strong className='cover__overlay__title'>{title}</strong>;\n   }\n+  // else render \"title (year)\"\n   const timestamp = new Date(parseInt(released, 10));\n-  return <strong className='cover__overlay__title'>{`\${title} (\${timestamp.getFullYear()})`}</strong>;\n+  const year = timestamp.getFullYear();\n+  return <strong className='cover__overlay__title'>{`\${title} (\${year})`}</strong>;\n };\n-\n+/**\n+ * Renders a div with a thumbnail. If no thumbnail is passed as a prop\n+ * it renders a default image.\n+ */\n const CoverImage = ({\n   thumbnail,\n   title,\n@@ -29,7 +42,6 @@ const CoverImage = ({\n }) => <div className={`cover \${!thumbnail ? 'cover--inverse' : ''}`}>\n   <Link to={url}>\n     <img className='cover__thumbnail' src={thumbnail || defaultImage}/>\n-\n     <div className='cover__overlay'>\n       {getTitleAndYear(title, released)}\n       {getRating(rating)}\n",
        // Expected Lines Added
        21,
        // Expected Lines Removed
        9,
      ),
      // This has only one addition
      Arguments.of(
        "README.md",
        "@@ -358,3 +358,4 @@ location / {\n 7. Du kan verifisere at denne kjører ved å kalle `sudo docker-compose ps` og hente ut logger ved å kalle `sudo docker-compose logs`.\n \n Applikasjonen vil etter dette være tilgjengelig på endepunktet `http://it2810-20.idi.ntnu.no/`. Internt på serveren kan du også pinge applikasjonen på `localhost:3000`. Får du ikke umiddelbar kontakt med nettsiden, kan du prøve å kalle `sudo docker-compose restart app`, for å tvinge nginx til å oppdage den interne serveren igjen.\n+\n",
        1,
        0,
      ),
    )
  }
}
