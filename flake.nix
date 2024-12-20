{
    inputs.nixpkgs.url = "nixpkgs/nixos-24.11";
    outputs = { self, nixpkgs, ... }: let
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
