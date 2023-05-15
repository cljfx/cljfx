(ns cljfx.jdk.fx)

(def keyword->lifecycle-delay
  {:spot-light (delay @(requiring-resolve 'cljfx.jdk.fx.spot-light/lifecycle))
   :directional-light (delay @(requiring-resolve 'cljfx.jdk.fx.directional-light/lifecycle))})
