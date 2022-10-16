package webapp.components.icons

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

def iconCopy(using services: Services) =
  import svg.* 
  svg(
    viewBox := "0 0 21 25",
    g(path(d := """M17.3125 0H6.375C5.5462 0 4.75134 0.32924 4.16529 0.915291C3.57924 1.50134 3.25 2.2962 3.25 3.125C2.4212 3.125 1.62634 3.45424 1.04029 4.04029C0.45424 4.62634 0.125 5.4212 0.125 6.25V21.875C0.125 22.7038 0.45424 23.4987 1.04029 24.0847C1.62634 24.6708 2.4212 25 3.25 25H14.1875C15.0163 25 15.8112 24.6708 16.3972 24.0847C16.9833 23.4987 17.3125 22.7038 17.3125 21.875C18.1413 21.875 18.9362 21.5458 19.5222 20.9597C20.1083 20.3737 20.4375 19.5788 20.4375 18.75V3.125C20.4375 2.2962 20.1083 1.50134 19.5222 0.915291C18.9362 0.32924 18.1413 0 17.3125 0ZM17.3125 20.3125V6.25C17.3125 5.4212 16.9833 4.62634 16.3972 4.04029C15.8112 3.45424 15.0163 3.125 14.1875 3.125H4.8125C4.8125 2.7106 4.97712 2.31317 5.27015 2.02015C5.56317 1.72712 5.9606 1.5625 6.375 1.5625H17.3125C17.7269 1.5625 18.1243 1.72712 18.4174 2.02015C18.7104 2.31317 18.875 2.7106 18.875 3.125V18.75C18.875 19.1644 18.7104 19.5618 18.4174 19.8549C18.1243 20.1479 17.7269 20.3125 17.3125 20.3125ZM1.6875 6.25C1.6875 5.8356 1.85212 5.43817 2.14515 5.14515C2.43817 4.85212 2.8356 4.6875 3.25 4.6875H14.1875C14.6019 4.6875 14.9993 4.85212 15.2924 5.14515C15.5854 5.43817 15.75 5.8356 15.75 6.25V21.875C15.75 22.2894 15.5854 22.6868 15.2924 22.9799C14.9993 23.2729 14.6019 23.4375 14.1875 23.4375H3.25C2.8356 23.4375 2.43817 23.2729 2.14515 22.9799C1.85212 22.6868 1.6875 22.2894 1.6875 21.875V6.25Z""", fill := "white"))
  )
