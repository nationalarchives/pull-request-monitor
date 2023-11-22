package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github

class HeaderParserTest extends org.specs2.mutable.Specification {
  "the page link header parser" should {
    "extract the 'next' link if it is the only link" in {
      val header = "<some-url>; rel=\"next\""
      val links = HeaderParser.parsePageLinks(header)

      links.next must beSome("some-url")
    }

    "ignore other types of links" in {
      val header = "<some-url>; rel=\"last\""
      val links = HeaderParser.parsePageLinks(header)

      links.next must beNone
    }

    "extract the 'next' link from a group of links" in {
      val header =
        "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=34>; rel=\"last\",  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=15>; rel=\"next\",  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=1>; rel=\"first\",  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=13>; rel=\"prev\""
      val links = HeaderParser.parsePageLinks(header)

      links.next must beSome("https://api.github.com/search/code?q=addClass+user%3Amozilla&page=15")
    }

    // The GitHub pagination spec (https://developer.github.com/v3/guides/traversing-with-pagination/) implies that
    // there could be line breaks in the link header
    "extract the 'next' link from a group of links with line breaks" in {
      val header =
        "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=34>; rel=\"last\",\n  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=15>; rel=\"next\",\n  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=1>; rel=\"first\",\n  <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=13>; rel=\"prev\""
      val links = HeaderParser.parsePageLinks(header)

      links.next must beSome("https://api.github.com/search/code?q=addClass+user%3Amozilla&page=15")
    }

    "reject an invalid header" in {
      val header = "some text that doesn't match the expected structure"

      HeaderParser.parsePageLinks(header) must throwA[RuntimeException]
    }
  }
}
