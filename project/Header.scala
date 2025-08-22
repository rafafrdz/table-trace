import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{HeaderLicense, headerLicense}
import de.heikoseeberger.sbtheader.License
import sbt._

object Header {

  def projectSettings: Seq[Setting[_]] = Seq(
    headerLicense := Some(headerIOLicense)
  )

  /**
   * SBT Header Plugin
   */

  lazy val headerText: String =
    """Copyright 2024 Rafael Fernandez
      |
      |Licensed under the Apache License, Version 2.0 (the "License");
      |you may not use this file except in compliance with the License.
      |You may obtain a copy of the License at
      |
      |http://www.apache.org/licenses/LICENSE-2.0
      |
      |Unless required by applicable law or agreed to in writing, software
      |distributed under the License is distributed on an "AS IS" BASIS,
      |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      |See the License for the specific language governing permissions and
      |limitations under the License.
      |""".stripMargin

  lazy val headerIOLicense: License.Custom =
    HeaderLicense.Custom(headerText)

}
