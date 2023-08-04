import dev.smithers.envrc

plugins {
     id("dev.smithers.envrc")
}

println("kotlin envrc val ${envrc["sdf"]}")
println("kotlin envrc val ${envrc.get("sdf")}")
println("kotlin envrc val ${envrc["none"]}")