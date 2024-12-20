{
    inputs = {
        nixpkgs.url = "nixpkgs/nixos-24.11";
        gradle2nix = {
            url = "github:tadfisher/gradle2nix/v2";
            inputs.nixpkgs.follows = "nixpkgs";
        };
    };
    outputs = { self, nixpkgs, gradle2nix, ... }: let
        system = "x86_64-linux";
        pkgs = import nixpkgs { inherit system; };
    in {
        devShells.${system}.default = pkgs.mkShellNoCC {
            packages = with pkgs; [
                gradle
            ];
            
        };
        packages.${system} = rec {
            source = gradle2nix.builders.x86_64-linux.buildGradlePackage rec {
                name = "folkevognen";
                version = "1.0";
                pname = "folkevognen";
                lockFile = ./gradle.lock;
                src = ./.;
                gradleBuildFlags = ["build -x test"];
                gradleInstallFlags = ["installDist -x test"];

                installPhase = ''
                    mkdir -p $out/{lib,share}/${name}
                    cp ./app/build/libs/app-all.jar $out/lib/${name}/folkevognen.jar
                    cp -r $src/* $out/share/${name}
                '';
            };
            default = pkgs.writeScriptBin "folkevognen" ''
                #!${pkgs.bash}/bin/bash
                ${pkgs.jdk21}/bin/java --enable-preview -jar ${source}/lib/folkevognen/folkevognen.jar $@
            '';
        };
    };
}
