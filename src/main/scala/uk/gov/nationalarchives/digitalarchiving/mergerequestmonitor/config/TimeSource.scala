package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config

import java.time.{ZoneId, ZonedDateTime}

/** A clock that can be queried for the time. This is mainly useful so that a fixed time can be injected by tests.
  */
trait TimeSource {
  def now: ZonedDateTime
}

class CurrentTimeSource extends TimeSource {
  override def now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
}
