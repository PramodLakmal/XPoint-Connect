using System.ComponentModel.DataAnnotations;

namespace XPoint_Connect_API.DTOs
{
    // EV Owner DTOs
    public class CreateEVOwnerDto
    {
        [Required]
        [RegularExpression(@"^[0-9]{9}[vVxX]$|^[0-9]{12}$", ErrorMessage = "Invalid NIC format")]
        public string NIC { get; set; } = string.Empty;

        [Required]
        public string FirstName { get; set; } = string.Empty;

        [Required]
        public string LastName { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        [Phone]
        public string PhoneNumber { get; set; } = string.Empty;

        public string Address { get; set; } = string.Empty;

        [Required]
        [MinLength(6)]
        public string Password { get; set; } = string.Empty;
    }

    public class UpdateEVOwnerDto
    {
        public string? FirstName { get; set; }
        public string? LastName { get; set; }
        public string? Email { get; set; }
        public string? PhoneNumber { get; set; }
        public string? Address { get; set; }
        public string? Password { get; set; }
        public bool? IsActive { get; set; }
        public bool? RequiresReactivation { get; set; }
    }

    public class EVOwnerResponseDto
    {
        public string NIC { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string FullName => $"{FirstName} {LastName}";
        public string Email { get; set; } = string.Empty;
        public string PhoneNumber { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public bool RequiresReactivation { get; set; }
    }
}