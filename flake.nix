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
    };
}
