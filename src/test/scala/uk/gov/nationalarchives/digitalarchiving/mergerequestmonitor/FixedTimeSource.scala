package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.TimeSource

import java.time.{ZoneId, ZonedDateTime}

class FixedTimeSource extends TimeSource {
  override def now: ZonedDateTime = ZonedDateTime.of(2021, 7, 8, 0, 0, 0, 0, ZoneId.of("UTC"))
}