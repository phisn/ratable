package webapp.application.pages

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class PrivacyPage() extends Page:
  def render(using services: Services) =
    layoutComponent(
      contentHorizontalCenterComponent(
        div(
          cls := "space-y-4",
          h1(cls := "text-3xl", """Privacy Policy of Ratable"""),
          p( """Ratable operates the https://ratable.schmisn.com/ website, which provides the SERVICE."""),
          p("""This page is used to inform website visitors regarding our policies with the collection, use, and disclosure of Personal Information if anyone decided to use our Service, the Ratable website."""),
          p(
            """If you choose to use our Service, then you agree to the collection and use of information in relation with this policy. The Personal Information that we collect are used for providing and improving the Service. We will not use or share your information with anyone except as described in this Privacy Policy. Our Privacy Policy was created with the help of the""",
            a(href := "https://www.privacypolicytemplate.net/", "Privacy Policy Template Generator"),
            """"."""),
          p("""The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at https://ratable.schmisn.com/, unless otherwise defined in this Privacy Policy."""),
          h2(cls := "text-xl", """Information Collection and Use"""),
          p("""For a better experience while using our Service, we may require you to provide us with certain personally identifiable information, including but not limited to your name, phone number, and postal address. The information that we collect will be used to contact or identify you."""),
          h2(cls := "text-xl", """Log Data"""),
          p("""We want to inform you that whenever you visit our Service, we collect information that your browser sends to us that is called Log Data. This Log Data may include information such as your computer’s Internet Protocol ("IP") address, browser version, pages of our Service that you visit, the time and date of your visit, the time spent on those pages, and other statistics."""),
          h2(cls := "text-xl", """Cookies"""),
          p("""Cookies are files with small amount of data that is commonly used an anonymous unique identifier. These are sent to your browser from the website that you visit and are stored on your computer’s hard drive."""),
          p("""Our website uses these "cookies" to collection information and to improve our Service. You have the option to either accept or refuse these cookies, and know when a cookie is being sent to your computer. If you choose to refuse our cookies, you may not be able to use some portions of our Service."""),
          h2(cls := "text-xl", """Service Providers"""),
          p("""We may employ third-party companies and individuals due to the following reasons:"""),
          ul(
            cls := "pl-4", 
            li("""To facilitate our Service;"""),
            li("""To provide the Service on our behalf;"""),
            li("""To perform Service-related services; or"""),
            li("""To assist us in analyzing how our Service is used.""")
          ),
          p("""We want to inform our Service users that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose."""),
          h2(cls := "text-xl", """Security"""),
          p("""We value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and we cannot guarantee its absolute security."""),
          h2(cls := "text-xl", """Links to Other Sites"""),
          p("""Our Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by us. Therefore, we strongly advise you to review the Privacy Policy of these websites. We have no control over, and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services."""),
          p("""Children's Privacy"""),
          p("""Our Services do not address anyone under the age of 13. We do not knowingly collect personal identifiable information from children under 13. In the case we discover that a child under 13 has provided us with personal information, we immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so that we will be able to do necessary actions."""),
          h2(cls := "text-xl", """Changes to This Privacy Policy"""),
          p("""We may update our Privacy Policy from time to time. Thus, we advise you to review this page periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on this page. These changes are effective immediately, after they are posted on this page."""),
          h2(cls := "text-xl", """Contact Us"""),
          p("""If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us.""")
        )
      )
    )
